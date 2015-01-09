package ch.virtualid.synchronizer;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.Context;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.EntityClass;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
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
import ch.virtualid.service.CoreService;
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
    static final Actions MODULE = new Actions();
    
    @Override
    public void createTables(final @Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "action (entity " + EntityClass.FORMAT + " NOT NULL, service " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, " + AgentPermissions.FORMAT_NULL + ", " + Restrictions.FORMAT + ", agent " + Agent.FORMAT + ", recipient " + IdentifierClass.FORMAT + " NOT NULL, action " + Block.FORMAT + " NOT NULL, PRIMARY KEY (entity, service, time), INDEX(time), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Mapper.REFERENCE + ", " + AgentPermissions.REFERENCE + ", " + Restrictions.getForeignKeys(site) + ", FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ")");
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
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.create("entry.actions.module@virtualid.ch").load(TupleWrapper.TYPE, InternalNonHostIdentity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, Time.TYPE, AgentPermissions.TYPE, Restrictions.TYPE, Agent.NUMBER, HostIdentity.IDENTIFIER, Packet.SIGNATURE);
    
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
                final @Nonnull AgentPermissions permissions = AgentPermissions.getEmptyOrSingle(resultSet, 4);
                final @Nonnull Restrictions restrictions = Restrictions.get(account, resultSet, 6);
                @Nullable Long number = resultSet.getLong(11);
                if (resultSet.wasNull()) number = null;
                final @Nonnull Identifier recipient = IdentifierClass.get(resultSet, 12);
                final @Nonnull Block action = Block.get(Packet.SIGNATURE, resultSet, 13);
                entries.add(new TupleWrapper(MODULE_ENTRY, account.getIdentity().toBlockable(InternalNonHostIdentity.IDENTIFIER), service.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), time, permissions, restrictions, (number != null ? new Int64Wrapper(Agent.NUMBER, number) : null), recipient.toBlock().setType(HostIdentity.IDENTIFIER).toBlockable(), action.toBlockable()).toBlock());
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
                new AgentPermissions(tuple.getElementNotNull(3)).checkAreSingle().setEmptyOrSingle(preparedStatement, 4);
                new Restrictions(NonHostAccount.get(host, identity), tuple.getElementNotNull(4)).set(preparedStatement, 6); // The entity is wrong for services but it does not matter. (Correct would be Roles.getRole(host.getClient(), identity.toInternalPerson()).)
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
    
    
    /**
     * Returns the audit trail of the given entity for the given service from the given time.
     * 
     * @param entity the entity whose audit trail is to be returned.
     * @param service the service for which the audit trail is wanted.
     * @param lastTime the time of the last request for the audit trail.
     * @param permissions the permissions of the requesting agent.
     * @param restrictions the restrictions of the requesting agent.
     * @param agent the agent for which the audit trail is restricted.
     * 
     * @return the audit trail of the given entity for the given service from the given time.
     * 
     * @require agent == null || service.equals(CoreService.TYPE) : "The agent is null or the audit trail is requested for the core service.";
     */
    @Pure
    public static @Nonnull ResponseAudit getAudit(@Nonnull NonHostEntity entity, @Nonnull SemanticType service, @Nonnull Time lastTime, @Nonnull ReadonlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws SQLException {
        assert agent == null || service.equals(CoreService.TYPE) : "The agent is null or the audit trail is requested for the core service.";
        
        final @Nonnull Site site = entity.getSite();
        final @Nonnull StringBuilder SQL = new StringBuilder("SELECT ").append(Database.getConfiguration().GREATEST()).append("(COALESCE(MAX(time), 0), ").append(Database.getConfiguration().CURRENT_TIME()).append("), NULL FROM ").append(site).append("action UNION ");
        SQL.append("SELECT NULL, action FROM ").append(site).append("action a WHERE entity = ").append(entity).append(" AND service = ").append(service).append(" AND time > ").append(lastTime);
        
        SQL.append(" AND (type_writing IS NULL OR NOT type_writing");
        if (!permissions.canRead(AgentPermissions.GENERAL)) SQL.append(" AND type_writing IN ").append(permissions.allTypesToString());
        SQL.append(" OR type_writing");
        if (!permissions.canWrite(AgentPermissions.GENERAL)) SQL.append(" AND type_writing IN ").append(permissions.writeTypesToString());
        SQL.append(")");
        
        if (!restrictions.isClient()) SQL.append(" AND NOT client");
        if (!restrictions.isRole()) SQL.append(" AND NOT role");
        if (!restrictions.isWriting()) SQL.append(" AND NOT context_writing");
        
        final @Nullable Context context = restrictions.getContext();
        final @Nullable Contact contact = restrictions.getContact();
        if (context == null) {
            SQL.append(" AND context IS NULL");
            if (contact == null) SQL.append(" AND contact IS NULL");
            else SQL.append(" AND (contact IS NULL OR contact = ").append(contact).append(")");
        } else {
            SQL.append(" AND (context IS NULL OR EXISTS (SELECT * FROM ").append(context.getEntity().getSite()).append("context_subcontext c WHERE c.entity = ").append(context.getEntity()).append(" AND c.context = ").append(context).append(" AND c.subcontext = a.context))");
            SQL.append(" AND (contact IS NULL OR EXISTS (SELECT * FROM ").append(context.getEntity().getSite()).append("context_subcontext cx, ").append(context.getEntity().getSite()).append("context_contact cc WHERE cx.entity = ").append(context.getEntity()).append(" AND cx.context = ").append(context).append(" AND cc.entity = ").append(context.getEntity()).append(" AND cc.context = cx.subcontext AND cc.contact = a.contact))");
        }
        
        SQL.append(" AND (agent IS NULL");
        if (agent != null) SQL.append(" OR EXISTS (SELECT * FROM ").append(site).append("agent_permission_order po, ").append(site).append("agent_restrictions_ord ro WHERE po.entity = ").append(entity).append(" AND po.stronger = ").append(agent).append(" AND po.weaker = a.agent AND ro.entity = ").append(entity).append(" AND ro.stronger = ").append(agent).append(" AND ro.weaker = a.agent)");
        SQL.append(") ORDER BY time ASC");
        
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL.toString())) {
            if (resultSet.next()) {
                final @Nonnull Time thisTime = Time.get(resultSet, 1);
                final @Nonnull FreezableList<Block> trail = new FreezableLinkedList<Block>();
                while (resultSet.next()) {
                    trail.add(Block.get(Packet.SIGNATURE, resultSet, 2));
                }
                return new ResponseAudit(lastTime, thisTime, trail.freeze());
            } else throw new SQLException("This should never happen.");
        }
    }
    
    /**
     * Adds the given action to the audit trail.
     * 
     * @param action the action to be added to the audit trail.
     * 
     * @require action.hasEntity() : "The action has an entity.";
     * @require action.hasSignature() : "The action has a signature.";
     */
    public static void audit(@Nonnull Action action) throws SQLException {
        assert action.hasEntity() : "The action has an entity.";
        assert action.hasSignature() : "The action has a signature.";
        
        final @Nonnull Entity entity = action.getEntityNotNull();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String TIME = Database.getConfiguration().GREATEST() + "(COALESCE(MAX(time), 0) + 1, " + Database.getConfiguration().CURRENT_TIME() + ")";
        final @Nonnull String SQL = "INSERT INTO " + site + "action (entity, service, time, " + AgentPermissions.COLUMNS + ", " + Restrictions.COLUMNS + ", agent, recipient, action) SELECT ?, ?, " + TIME + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM " + site + "action";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            entity.set(preparedStatement, 1);
            action.getService().getType().set(preparedStatement, 2);
            action.getAuditPermissions().setEmptyOrSingle(preparedStatement, 3);
            action.getAuditRestrictions().set(preparedStatement, 5);
            Agent.set(action.getAuditAgent(), preparedStatement, 10);
            action.getRecipient().set(preparedStatement, 11);
            action.getSignatureNotNull().toBlock().set(preparedStatement, 12);
            preparedStatement.executeUpdate();
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
