package ch.virtualid.module.host;

import ch.virtualid.authorization.Agent;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.concept.Context;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.database.Database;
import ch.virtualid.database.HostEntity;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.Packet;
import static ch.virtualid.server.Server.YEAR;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * This class provides database access to the actions of the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Actions {
    
    /**
     * Initializes the database by creating the appropriate tables if necessary.
     * 
     * @param connection an open host connection to the database.
     */
    public static void initialize(@Nonnull HostEntity connection) throws SQLException {
        try (@Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS action (identity BIGINT NOT NULL, time BIGINT NOT NULL, client BOOLEAN, role BOOLEAN, context BIGINT, contact BIGINT, type BIGINT, writing BOOLEAN, authorizationID BIGINT, request LONGBLOB NOT NULL, PRIMARY KEY (identity, time), INDEX(time), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (authorizationID) REFERENCES authorization (authorizationID), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))");
        }
        
        Mapper.addIdentityReference("action", "identity");
        Mapper.addTypeReference("action", "type");
        Mapper.addReference("action", "contact");
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM request WHERE time < UNIX_TIMESTAMP() * 1000 - " + YEAR);
                } catch (@Nonnull SQLException exception) {}
            }
        }, 0, YEAR);
    }
    
    /**
     * Returns the audit trail of the given identity from the given time restricted for the given agent.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity whose audit trail is to be returned.
     * @param time the time of the last audited request.
     * @param agent the agent for which the audit trail is to be restricted.
     * @return the audit trail of the given identity from the given time restricted for the given agent.
     */
    private static @Nonnull Audit getAudit(@Nonnull Connection connection, @Nonnull Identity identity, long time, @Nonnull Agent agent) throws SQLException {
        @Nullable Restrictions restrictions = agent.getRestrictions();
        if (restrictions != null && restrictions.getHistory() > time) time = restrictions.getHistory();
        
        @Nonnull StringBuilder query = new StringBuilder("SELECT MAX(time), request FROM request WHERE identity = ").append(identity).append(" AND time > ").append(time);
        if (restrictions == null) {
            query.append(" AND client IS NULL AND context IS NULL AND role IS NULL AND contact IS NULL AND type IS NULL AND writing IS NULL");
        } else {
            if (!restrictions.isClient()) query.append(" AND (client is NULL OR NOT client)");
            if (!restrictions.isRole()) query.append(" AND (role is NULL OR NOT role)");
            query.append(" AND (context IS NULL OR context & ").append(restrictions.getContext().getMask()).append(" = ").append(restrictions.getContext()).append(")");
            query.append(" AND (contact IS NULL OR contact IN (SELECT contact FROM context_contact WHERE identity = ").append(identity).append(" AND context & ").append(restrictions.getContext().getMask()).append(" = ").append(restrictions.getContext()).append("))");
            query.append(" AND (type IS NULL AND writing IS NULL OR EXISTS(SELECT * FROM authorization_permission WHERE authorizationID = ").append(agent).append(" AND NOT preference AND (authorization_permission.type = request.type OR authorization_permission.type = ").append(SemanticType.CLIENT_GENERAL_PERMISSION).append(") AND (NOT request.writing OR authorization_permission.writing)))");
        }
        query.append(" AND (authorizationID IS NULL OR authorizationID IN (SELECT weaker FROM agent_order WHERE stronger = ").append(agent).append(")) ORDER BY time");
        
        @Nonnull List<Block> trail = new LinkedList<Block>();
        try (@Nonnull Statement statement = connection.createStatement()) {
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT " + Database.CURRENT_TIME)) {
                if (resultSet.next()) time = resultSet.getLong(1);
            }
            try (@Nonnull ResultSet resultSet = statement.executeQuery(query.toString())) {
                while (resultSet.next()) {
                    long max = resultSet.getLong(1);
                    if (max > time) time = max;
                    trail.add(new Block(resultSet.getBytes(1)));
                }
            }
        }
        return new Audit(time, trail);
    }
    
    /**
     * Adds the given request to the audit trail of the given identity.
     * 
     * @param connection an open connection to the database.
     * @param identity the identity whose audit trail is extended.
     * @param client whether the modification of the request can only be seen by directly accredited clients (or null if it is irrelevant).
     * @param role whether the modification of the request can only be seen by agents that can assume roles (or null if it is irrelevant).
     * @param context the context of the request or null.
     * @param contact the contact of the request or null.
     * @param type the attribute type of the request or null.
     * @param writing the attribute permission of the request or null.
     * @param agent the agent modified in the request or null.
     * @param request the request to be added to the audit trail.
     */
    private static void addToAuditTrail(@Nonnull Connection connection, @Nonnull NonHostIdentity identity, @Nullable Boolean client, @Nullable Boolean role, @Nullable Context context, @Nullable Person contact, @Nullable SemanticType type, @Nullable Boolean writing, @Nullable Agent agent, @Nonnull Packet request) throws SQLException {
        @Nonnull String time = Database.GREATEST + "(MAX(time) + 1, " + Database.CURRENT_TIME + ")";
        @Nonnull String statement = "INSERT INTO request (identity, time, client, role, context, contact, type, writing, agent, request) SELECT ?, " + time + ", ?, ?, ?, ?, ?, ?, ?, ? FROM request";
        try (@Nonnull PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setLong(1, identity.getNumber());
            if (client == null) { preparedStatement.setNull(2, java.sql.Types.BOOLEAN);} else { preparedStatement.setBoolean(2, client); }
            if (role == null) { preparedStatement.setNull(3, java.sql.Types.BOOLEAN);} else { preparedStatement.setBoolean(3, role); }
            if (context == null) { preparedStatement.setNull(4, java.sql.Types.BIGINT); } else { preparedStatement.setLong(4, context.getNumber()); }
            if (contact == null) { preparedStatement.setNull(5, java.sql.Types.BIGINT); } else { preparedStatement.setLong(5, contact.getNumber()); }
            if (type == null) { preparedStatement.setNull(6, java.sql.Types.BIGINT); } else { preparedStatement.setLong(6, type.getNumber()); }
            if (writing == null) { preparedStatement.setNull(7, java.sql.Types.BOOLEAN); } else { preparedStatement.setBoolean(7, writing); }
            if (agent == null) { preparedStatement.setNull(8, java.sql.Types.BIGINT); } else { preparedStatement.setLong(8, agent.getNumber()); }
            preparedStatement.setBytes(9, request.getSignatures().toBlock().getSection());
            preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Returns the given block with the requested audit or null if no agent is provided.
     * 
     * @param block the block containing the response of the request.
     * @param connection an open connection to the database.
     * @param identity the subject of the request.
     * @param signature the signature of the request.
     * @param agent the agent signing the request or null.
     * @return the given block with the requested audit or null if no agent is provided.
     */
    private static @Nonnull Pair<Block, Audit> withAudit(@Nonnull Block block, @Nonnull Connection connection, @Nonnull Identity identity, @Nonnull SignatureWrapper signature, @Nullable Agent agent) throws SQLException {
        @Nullable Audit audit = signature.getAudit();
        if (audit != null && agent != null) audit = getAudit(connection, identity, audit.getLastTime(), agent);
        return new Pair<Block, Audit>(block, audit);
    }
    
}
