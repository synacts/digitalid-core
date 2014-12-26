package ch.virtualid.module.both;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.InternalQuery;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Level;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.Packet;
import ch.virtualid.server.Host;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.EmptyWrapper;
import ch.xdf.Int64Wrapper;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides database access to the {@link InternalAction internal actions} of the core service.
 * This module does not support getting, adding and deleting an entity's state as this is not desired.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Actions implements BothModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final Actions MODULE = new Actions();
    
    @Override
    public void createTables(final @Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "action (entity " + EntityClass.FORMAT + " NOT NULL, service " + Mapper.FORMAT + " NOT NULL, time BIGINT NOT NULL, " + Restrictions.FORMAT + ", " + AgentPermissions.FORMAT_NULL + ", agent " + Agent.FORMAT + ", recipient " + IdentifierClass.FORMAT + " NOT NULL, action " + Database.getConfiguration().BLOB() + " NOT NULL, PRIMARY KEY (entity, service, time), INDEX(time), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Mapper.REFERENCE + ", " + Restrictions.getForeignKeys(site) + ", " + AgentPermissions.REFERENCE + ", FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ")");
            Mapper.addReference(site + "action", "contact");
            if (site instanceof Host) Mapper.addReference(site + "action", "entity", "entity", "time");
        }
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (@Nonnull Statement statement = Database.createStatement()) {
                    statement.executeUpdate("DELETE FROM " + site + "action WHERE time < " + Time.TROPICAL_YEAR.ago());
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Database.LOGGER.log(Level.WARNING, exception);
                }
            }
        }, Time.HOUR.getValue(), Time.MONTH.getValue());
    }
    
    @Override
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            if (site instanceof Host) Mapper.removeReference(site + "action", "entity", "entity", "time");
            Mapper.removeReference(site + "action", "contact");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "action");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.actions.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.actions.module@virtualid.ch").load(TupleWrapper.TYPE, InternalNonHostIdentity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, Time.TYPE, Restrictions.TYPE, AgentPermissions.TYPE, Agent.NUMBER, HostIdentity.IDENTIFIER, Packet.SIGNATURE);
    
    /**
     * Stores the semantic type {@code actions.module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.create("actions.module@virtualid.ch").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull String SQL = "SELECT entity, service, time, " + Restrictions.COLUMNS + ", " + AgentPermissions.COLUMNS + ", agent, recipient, action FROM " + host + "action";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<Block>();
            while (resultSet.next()) {
                final @Nonnull NonHostAccount account = NonHostAccount.getNotNull(host, resultSet, 1);
                final @Nonnull Identity service = IdentityClass.getNotNull(resultSet, 2);
                final @Nonnull Time time = Time.get(resultSet, 3);
                final @Nonnull Restrictions restrictions = Restrictions.get(account, resultSet, 4);
                final @Nonnull AgentPermissions permissions = AgentPermissions.getEmptyOrSingle(resultSet, 9);
                @Nullable Long number = resultSet.getLong(11);
                if (resultSet.wasNull()) number = null;
                final @Nonnull Identifier recipient = IdentifierClass.get(resultSet, 12);
                final @Nonnull Block action = Block.get(Packet.SIGNATURE, resultSet, 13);
                entries.add(new TupleWrapper(MODULE_ENTRY, account.getIdentity().toBlockable(InternalNonHostIdentity.IDENTIFIER), service.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), time, restrictions, permissions, (number != null ? new Int64Wrapper(Agent.NUMBER, number) : null), recipient.toBlock().setType(HostIdentity.IDENTIFIER).toBlockable(), action.toBlockable()).toBlock());
            }
            return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
        }
    }
    
    @Override
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement("INSERT INTO " + host + "action (entity, service, time, " + Restrictions.COLUMNS + ", " + AgentPermissions.COLUMNS + ", agent, recipient, action) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadonlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull TupleWrapper tuple = new TupleWrapper(entry);
                final @Nonnull InternalNonHostIdentity identity = IdentityClass.create(tuple.getElementNotNull(0)).toInternalNonHostIdentity();
                identity.set(preparedStatement, 1);
                IdentityClass.create(tuple.getElementNotNull(1)).toSemanticType().set(preparedStatement, 2);
                new Time(tuple.getElementNotNull(2)).set(preparedStatement, 3);
                new Restrictions(NonHostAccount.get(host, identity), tuple.getElementNotNull(3)).set(preparedStatement, 4); // The entity is wrong for services but it does not matter. (Correct would be Roles.getRole(host.getClient(), identity.toInternalPerson()).)
                new AgentPermissions(tuple.getElementNotNull(4)).checkAreSingle().setEmptyOrSingle(preparedStatement, 9);
                if (tuple.isElementNull(5)) preparedStatement.setLong(11, new Int64Wrapper(tuple.getElementNotNull(5)).getValue());
                else preparedStatement.setNull(11, Types.BIGINT);
                IdentifierClass.create(tuple.getElementNotNull(6)).toHostIdentifier().set(preparedStatement, 12);
                tuple.getElementNotNull(7).set(preparedStatement, 13);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code actions.state@virtualid.ch}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.create("actions.state@virtualid.ch").load(EmptyWrapper.TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull Agent agent) throws SQLException {
        return new EmptyWrapper(STATE_FORMAT).toBlock();
    }
    
    @Override
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
    }
    
    @Override
    public void removeState(@Nonnull NonHostEntity entity) throws SQLException {}
    
    @Pure
    @Override
    public @Nullable InternalQuery getInternalQuery(@Nonnull Role role) {
        return null;
    }
    
    
    /**
     * Returns the audit trail of the given identity from the given time restricted for the given agent.
     * 
     * @param entity the identity whose audit trail is to be returned.
     * @param time the time of the last audited request.
     * @param agent the agent for which the audit trail is to be restricted.
     * 
     * @return the audit trail of the given identity from the given time restricted for the given agent.
     */
    @Pure
    public static @Nonnull Audit getAudit(@Nonnull NonHostEntity entity, @Nonnull SemanticType service, @Nonnull Time time, @Nonnull Restrictions restrictions, @Nonnull ReadonlyAgentPermissions permissions, @Nullable Agent agent) throws SQLException {
        @Nonnull StringBuilder query = new StringBuilder("SELECT MAX(time), request FROM request WHERE identity = ").append(entity).append(" AND time > ").append(time);
        if (restrictions == null) {
            query.append(" AND client IS NULL AND context IS NULL AND role IS NULL AND contact IS NULL AND type IS NULL AND writing IS NULL");
        } else {
            if (!restrictions.isClient()) query.append(" AND (client is NULL OR NOT client)");
            if (!restrictions.isRole()) query.append(" AND (role is NULL OR NOT role)");
            query.append(" AND (context IS NULL OR context & ").append(restrictions.getContext().getMask()).append(" = ").append(restrictions.getContext()).append(")");
            query.append(" AND (contact IS NULL OR contact IN (SELECT contact FROM context_contact WHERE identity = ").append(entity).append(" AND context & ").append(restrictions.getContext().getMask()).append(" = ").append(restrictions.getContext()).append("))");
            query.append(" AND (type IS NULL AND writing IS NULL OR EXISTS(SELECT * FROM authorization_permission WHERE authorizationID = ").append(agent).append(" AND NOT preference AND (authorization_permission.type = request.type OR authorization_permission.type = ").append(SemanticType.CLIENT_GENERAL_PERMISSION).append(") AND (NOT request.writing OR authorization_permission.writing)))");
        }
        query.append(" AND (authorizationID IS NULL OR authorizationID IN (SELECT weaker FROM agent_order WHERE stronger = ").append(agent).append(")) ORDER BY time");
        
        @Nonnull List<Block> trail = new LinkedList<Block>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT " + Database.getConfiguration().CURRENT_TIME())) {
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
     * Adds the given internal action to the audit trail.
     * 
     * @param action the action to be added to the audit trail.
     */
    public static void audit(@Nonnull InternalAction action) throws SQLException {
        @Nonnull String time = Database.getConfiguration().GREATEST() + "(MAX(time) + 1, " + Database.getConfiguration().CURRENT_TIME() + ")";
        @Nonnull String statement = "INSERT INTO request (identity, time, client, role, context, contact, type, writing, agent, request) SELECT ?, " + time + ", ?, ?, ?, ?, ?, ?, ?, ? FROM request";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(statement)) {
            preparedStatement.setLong(1, entity.getNumber());
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
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
