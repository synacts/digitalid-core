package net.digitalid.service.core.action.synchronizer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.site.Site;
import net.digitalid.service.core.CoreService;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.EmptyWrapper;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.concepts.agent.Restrictions;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.concepts.contact.Context;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.EntityImplementation;
import net.digitalid.service.core.entity.NonHostAccount;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.IdentityImplementation;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.packet.Packet;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;

/**
 * This class provides database access to the {@link InternalAction internal actions} of the core service.
 * This module does not support getting, adding and deleting an entity's state as this is not desired.
 */
@Stateless
public final class ActionModule implements StateModule {
    
    /**
     * Stores an instance of this module.
     */
    static final ActionModule MODULE = new ActionModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(final @Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "action (entity " + EntityImplementation.FORMAT + " NOT NULL, service " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, " + FreezableAgentPermissions.FORMAT_NULL + ", " + Restrictions.FORMAT + ", agent " + Agent.FORMAT + ", recipient " + IdentifierImplementation.FORMAT + " NOT NULL, action " + Block.FORMAT + " NOT NULL, PRIMARY KEY (entity, service, time), FOREIGN KEY (entity) " + site.getEntityReference() + ", FOREIGN KEY (service) " + Mapper.REFERENCE + Database.getConfiguration().INDEX("time") + ", " + FreezableAgentPermissions.REFERENCE + ", " + Restrictions.getForeignKeys(site) + ", FOREIGN KEY (entity, agent) " + Agent.getReference(site) + ")");
            Database.getConfiguration().createIndex(statement, site + "action", "time");
            Mapper.addReference(site + "action", "contact");
            if (site instanceof Host) { Mapper.addReference(site + "action", "entity", "entity", "service", "time"); }
            Database.addRegularPurging(site + "action", Time.TROPICAL_YEAR);
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.removeRegularPurging(site + "action");
            if (site instanceof Host) { Mapper.removeReference(site + "action", "entity", "entity", "service", "time"); }
            Mapper.removeReference(site + "action", "contact");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "action");
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.actions.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.actions.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, InternalNonHostIdentity.IDENTIFIER, SemanticType.ATTRIBUTE_IDENTIFIER, Time.TYPE, FreezableAgentPermissions.TYPE, Restrictions.TYPE, Agent.NUMBER, HostIdentity.IDENTIFIER, Packet.SIGNATURE);
    
    /**
     * Stores the semantic type {@code actions.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("actions.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
        final @Nonnull String SQL = "SELECT entity, service, time, " + Restrictions.COLUMNS + ", " + FreezableAgentPermissions.COLUMNS + ", agent, recipient, action FROM " + host + "action";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final @Nonnull NonHostAccount account = NonHostAccount.getNotNull(host, resultSet, 1);
                final @Nonnull Identity service = IdentityImplementation.getNotNull(resultSet, 2);
                final @Nonnull Time time = Time.get(resultSet, 3);
                final @Nonnull FreezableAgentPermissions permissions = FreezableAgentPermissions.getEmptyOrSingle(resultSet, 4);
                final @Nonnull Restrictions restrictions = Restrictions.get(account, resultSet, 6);
                @Nullable Long number = resultSet.getLong(11);
                if (resultSet.wasNull()) { number = null; }
                final @Nonnull Identifier recipient = IdentifierImplementation.get(resultSet, 12);
                final @Nonnull Block action = Block.getNotNull(Packet.SIGNATURE, resultSet, 13);
                entries.add(TupleWrapper.encode(MODULE_ENTRY, account.getIdentity().toBlockable(InternalNonHostIdentity.IDENTIFIER), service.toBlockable(SemanticType.ATTRIBUTE_IDENTIFIER), time, permissions, restrictions, (number != null ? Int64Wrapper.encode(Agent.NUMBER, number) : null), recipient.toBlock().setType(HostIdentity.IDENTIFIER).toBlockable(), action.toBlockable()));
            }
            return ListWrapper.encode(MODULE_FORMAT, entries.freeze());
        }
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement("INSERT INTO " + host + "action (entity, service, time, " + Restrictions.COLUMNS + ", " + FreezableAgentPermissions.COLUMNS + ", agent, recipient, action) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull TupleWrapper tuple = TupleWrapper.decode(entry);
                final @Nonnull InternalNonHostIdentity identity = IdentityImplementation.create(tuple.getNonNullableElement(0)).castTo(InternalNonHostIdentity.class);
                identity.set(preparedStatement, 1);
                IdentityImplementation.create(tuple.getNonNullableElement(1)).castTo(SemanticType.class).set(preparedStatement, 2);
                Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(2)).set(preparedStatement, 3);
                new FreezableAgentPermissions(tuple.getNonNullableElement(3)).checkIsSingle().setEmptyOrSingle(preparedStatement, 4);
                new Restrictions(NonHostAccount.get(host, identity), tuple.getNonNullableElement(4)).set(preparedStatement, 6); // The entity is wrong for services but it does not matter. (Correct would be Roles.getRole(host.getClient(), identity.castTo(InternalPerson.class)).)
                if (tuple.isElementNull(5)) { preparedStatement.setLong(11, Int64Wrapper.decode(tuple.getNonNullableElement(5))); }
                else { preparedStatement.setNull(11, Types.BIGINT); }
                IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(6)).castTo(HostIdentifier.class).set(preparedStatement, 12);
                tuple.getNonNullableElement(7).set(preparedStatement, 13);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    
    /**
     * Stores the semantic type {@code actions.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("actions.state@core.digitalid.net").load(EmptyWrapper.XDF_TYPE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        return EmptyWrapper.encode(STATE_FORMAT);
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, InvalidEncodingException {
        assert block.getType().isBasedOn(getStateFormat()) : "The block is based on the indicated type.";
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {}
    
    
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
    @NonCommitting
    public static @Nonnull ResponseAudit getAudit(@Nonnull NonHostEntity entity, @Nonnull Service service, @Nonnull Time lastTime, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        assert agent == null || service.equals(CoreService.SERVICE) : "The agent is null or the audit trail is requested for the core service.";
        
        final @Nonnull Site site = entity.getSite();
        final @Nonnull StringBuilder SQL = new StringBuilder("(SELECT ").append(Database.getConfiguration().GREATEST()).append("(COALESCE(MAX(time), 0), ").append(Database.getConfiguration().CURRENT_TIME()).append("), NULL FROM ").append(site).append("action) UNION ");
        SQL.append("(SELECT time, action FROM ").append(site).append("action a WHERE entity = ").append(entity).append(" AND service = ").append(service.getType()).append(" AND time > ").append(lastTime);
        SQL.append(" AND (type_writing IS NULL OR NOT type_writing").append(permissions.allTypesToString()).append(" OR type_writing").append(permissions.writeTypesToString()).append(")");
        
        if (!restrictions.isClient()) { SQL.append(" AND NOT client"); }
        if (!restrictions.isRole()) { SQL.append(" AND NOT role"); }
        if (!restrictions.isWriting()) { SQL.append(" AND NOT context_writing"); }
        
        final @Nullable Context context = restrictions.getContext();
        final @Nullable Contact contact = restrictions.getContact();
        if (context == null) {
            SQL.append(" AND context IS NULL");
            if (contact == null) { SQL.append(" AND contact IS NULL"); }
            else { SQL.append(" AND (contact IS NULL OR contact = ").append(contact).append(")"); }
        } else {
            SQL.append(" AND (context IS NULL OR EXISTS (SELECT * FROM ").append(context.getEntity().getSite()).append("context_subcontext c WHERE c.entity = ").append(context.getEntity()).append(" AND c.context = ").append(context).append(" AND c.subcontext = a.context))");
            SQL.append(" AND (contact IS NULL OR EXISTS (SELECT * FROM ").append(context.getEntity().getSite()).append("context_subcontext cx, ").append(context.getEntity().getSite()).append("context_contact cc WHERE cx.entity = ").append(context.getEntity()).append(" AND cx.context = ").append(context).append(" AND cc.entity = ").append(context.getEntity()).append(" AND cc.context = cx.subcontext AND cc.contact = a.contact))");
        }
        
        SQL.append(" AND (agent IS NULL");
        if (agent != null) { SQL.append(" OR EXISTS (SELECT * FROM ").append(site).append("agent_permission_order po, ").append(site).append("agent_restrictions_ord ro WHERE po.entity = ").append(entity).append(" AND po.stronger = ").append(agent).append(" AND po.weaker = a.agent AND ro.entity = ").append(entity).append(" AND ro.stronger = ").append(agent).append(" AND ro.weaker = a.agent)"); }
        SQL.append(") ORDER BY time ASC)");
        
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL.toString())) {
            if (resultSet.next()) {
                final @Nonnull Time thisTime = Time.SQL_CONVERTER.restoreNonNullable(None.OBJECT, resultSet, 1);
                final @Nonnull FreezableList<Block> trail = FreezableLinkedList.get();
                while (resultSet.next()) {
                    trail.add(Block.SQL_CONVERTER.restoreNonNullable(Packet.SIGNATURE, resultSet, 2));
                }
                return new ResponseAudit(lastTime, thisTime, trail.freeze());
            } else { throw new SQLException("This should never happen."); }
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
    @NonCommitting
    public static void audit(@Nonnull Action action) throws DatabaseException {
        assert action.hasEntity() : "The action has an entity.";
        assert action.hasSignature() : "The action has a signature.";
        
        final @Nonnull Entity entity = action.getEntityNotNull();
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String TIME = Database.getConfiguration().GREATEST() + "(COALESCE(MAX(time), 0) + 1, " + Database.getConfiguration().CURRENT_TIME() + ")";
        final @Nonnull String SQL = "INSERT INTO " + site + "action (entity, service, time, " + FreezableAgentPermissions.COLUMNS + ", " + Restrictions.COLUMNS + ", agent, recipient, action) SELECT ?, ?, " + TIME + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM " + site + "action";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            entity.set(preparedStatement, 1);
            action.getService().set(preparedStatement, 2);
            action.getRequiredPermissionsToSeeAudit().setEmptyOrSingle(preparedStatement, 3);
            action.getRequiredRestrictionsToSeeAudit().set(preparedStatement, 5);
            Agent.set(action.getRequiredAgentToSeeAudit(), preparedStatement, 10);
            action.getRecipient().set(preparedStatement, 11);
            action.getSignatureNotNull().toBlock().set(preparedStatement, 12);
            preparedStatement.executeUpdate();
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
