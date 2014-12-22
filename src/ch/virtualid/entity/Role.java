package ch.virtualid.entity;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.query.internal.StateQuery;
import ch.virtualid.handler.reply.query.StateReply;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.client.Roles;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a role on the client-side.
 * 
 * @see Roles
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Role extends EntityClass implements NonHostEntity, Immutable, SQLizable, Observer {
    
    /**
     * Stores the aspect of a new role being added to the observed role.
     */
    public static final @Nonnull Aspect ADDED = new Aspect(Role.class, "added");
    
    
    /**
     * Stores the semantic type {@code issuer.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType ISSUER = SemanticType.create("issuer.role@virtualid.ch").load(InternalNonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code relation.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType RELATION = SemanticType.create("relation.role@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code agent.role@virtualid.ch}.
     */
    public static final @Nonnull SemanticType AGENT = SemanticType.create("agent.role@virtualid.ch").load(Agent.NUMBER);
    
    
    /**
     * Stores the client that can assume this role.
     */
    private final @Nonnull Client client;
    
    /**
     * Stores the number that references this role.
     */
    private final long number;
    
    /**
     * Stores the issuer of this role.
     */
    private final @Nonnull InternalNonHostIdentity issuer;
    
    /**
     * Stores the relation of this role.
     * 
     * @invariant relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    private final @Nullable SemanticType relation;
    
    /**
     * Stores the recipient of this role.
     */
    private final @Nullable Role recipient;
    
    /**
     * Stores the agent of this role.
     */
    private final @Nonnull Agent agent;
    
    /**
     * Creates a new role for the given client with the given number, issuer, relation, recipient and agent.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     * @param relation the relation of the new role.
     * @param recipient the recipient of the new role.
     * @param agentNumber the agent number of the new role.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    private Role(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, long agentNumber) {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        this.client = client;
        this.number = number;
        this.issuer = issuer;
        this.relation = relation;
        this.recipient = recipient;
        this.agent = Agent.get(this, agentNumber, recipient == null, false);
    }
    
    /**
     * Returns the client that can assume this role.
     * 
     * @return the client that can assume this role.
     */
    @Pure
    public @Nonnull Client getClient() {
        return client;
    }
    
    /**
     * Returns the issuer of this role.
     * 
     * @return the issuer of this role.
     */
    @Pure
    public @Nonnull InternalNonHostIdentity getIssuer() {
        return issuer;
    }
    
    /**
     * Returns the relation of this role.
     * 
     * @return the relation of this role.
     * 
     * @ensure relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    @Pure
    public @Nullable SemanticType getRelation() {
        return relation;
    }
    
    /**
     * Returns the recipient of this role.
     * 
     * @return the recipient of this role.
     */
    @Pure
    public @Nullable Role getRecipient() {
        return recipient;
    }
    
    /**
     * Returns the agent of this role.
     * 
     * @return the agent of this role.
     * 
     * @ensure isNative() == return instanceof ClientAgent : "In case this role is native, the returned agent is a client agent.";
     * @ensure isNotNative() == return instanceof OutgoingRole : "In case this role is not native, the returned agent is an outgoing role.";
     */
    @Pure
    public @Nonnull Agent getAgent() {
        return agent;
    }
    
    /**
     * Returns whether this role is native.
     * 
     * @return whether this role is native.
     */
    @Pure
    public boolean isNative() {
        return recipient == null;
    }
    
    /**
     * Returns whether this role is not native.
     * 
     * @return whether this role is not native.
     */
    @Pure
    public boolean isNotNative() {
        return recipient != null;
    }
    
    
    @Pure
    @Override
    public @Nonnull Client getSite() {
        return client;
    }
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity() {
        return issuer;
    }
    
    @Pure
    @Override
    public long getNumber() {
        return number;
    }
    
    
    /**
     * Reloads the state of this role.
     */
    public void reloadState() throws SQLException, IOException, PacketException, ExternalException {
        // TODO: Probably replace this with a method by the synchronizer which also adjusts the audit time.
        final @Nonnull StateReply reply = new StateQuery(this).sendNotNull();
        CoreService.SERVICE.removeState(this);
        CoreService.SERVICE.addState(this, reply.toBlock());
        Database.commit();
    }
    
    /**
     * Refreshes the state of this role.
     */
    public void refreshState() throws SQLException, IOException, PacketException, ExternalException {
        // TODO: Rewrite this method with an AuditQuery instead of a full StateQuery!
        reloadState();
    }
    
    /**
     * Returns whether this role is accredited.
     * If it is, the current state is retrieved.
     * 
     * @return whether this role is accredited.
     */
    public boolean isAccredited() throws SQLException, IOException, PacketException, ExternalException {
        try {
            reloadState();
            return true;
        } catch (@Nonnull PacketException exception) {
            if (exception.getError() == PacketError.AUTHORIZATION) return false;
            else throw exception;
        }
    }
    
    
    /**
     * Stores the roles of this role.
     * 
     * @invariant roles == null || roles.isNotFrozen() : "The roles are not frozen.";
     * @invariant roles == null || roles.doesNotContainNull() : "The roles do not contain null.";
     * @invariant roles == null || roles.doesNotContainDuplicates() : "The roles do not contain duplicates.";
     * @invariant roles == null || for (Role role : roles) role.isNotNative() : "Every role in the roles is not native.";
     */
    private @Nullable FreezableList<Role> roles;
    
    /**
     * Returns the roles of this role.
     * 
     * @return the roles of this role.
     * 
     * @ensure return.isNotFrozen() : "The returned list is not frozen.";
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     * @ensure return.doesNotContainDuplicates() : "The returned list does not contain duplicates.";
     * @ensure for (Role role : return) role.isNotNative() : "Every role in the returned list is not native.";
     */
    @Pure
    public @Nonnull ReadonlyList<Role> getRoles() throws SQLException {
        if (roles == null) roles = Roles.getRoles(this);
        return roles;
    }
    
    /**
     * Adds the given role to the roles of this role.
     * 
     * @param issuer the issuer of the role to add.
     * @param relation the relation of the role to add.
     * @param agentNumber the agent number of the role to add.
     * 
     * @require relation.isRoleType() : "The relation is a role type.";
     */
    @OnlyForActions
    public void addRole(@Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, long agentNumber) throws SQLException {
        assert relation.isRoleType() : "The relation is a role type.";
        
        final @Nonnull Role role = add(client, issuer, relation, this, agentNumber);
        role.observe(this, DELETED);
        
        if (roles != null && !roles.contains(role)) roles.add(role);
        notify(ADDED);
    }
    
    @Override
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
        if (aspect.equals(DELETED) && roles != null) roles.remove(instance);
    }
    
    /**
     * Removes this role.
     */
    public void remove() throws SQLException {
        remove(this);
        notify(DELETED);
    }
    
    
    /**
     * Caches the roles given their client and number.
     */
    private static final @Nonnull ConcurrentMap<Client, ConcurrentMap<Long, Role>> index = new ConcurrentHashMap<Client, ConcurrentMap<Long, Role>>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Client.DELETED);
        }
    }
    
    /**
     * Returns the potentially locally cached role with the given arguments.
     * 
     * @param client the client that can assume the returned role.
     * @param number the number that references the returned role.
     * @param issuer the issuer of the returned role.
     * @param relation the relation of the returned role.
     * @param recipient the recipient of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing role with the given arguments.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public static @Nonnull Role get(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, long agentNumber) throws SQLException {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Long, Role> map = index.get(client);
            if (map == null) map = index.putIfAbsentElseReturnPresent(client, new ConcurrentHashMap<Long, Role>());
            @Nullable Role role = map.get(number);
            if (role == null) role = map.putIfAbsentElseReturnPresent(number, new Role(client, number, issuer, relation, recipient, agentNumber));
            return role;
        } else {
            return new Role(client, number, issuer, relation, recipient, agentNumber);
        }
    }
    
    /**
     * Returns a new or existing role with the given arguments.
     * <p>
     * <em>Important:</em> This method should not be called directly.
     * (Use {@link #addRole(ch.virtualid.identity.InternalNonHostIdentity, ch.virtualid.identity.SemanticType, long)}
     * or {@link Client#addRole(ch.virtualid.identity.InternalNonHostIdentity)} instead.)
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param relation the relation of the returned role.
     * @param recipient the recipient of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing role with the given arguments.
     * 
     * @require relation == null || relation.isRoleType() : "The relation is either null or a role type.";
     */
    public static @Nonnull Role add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, @Nullable SemanticType relation, @Nullable Role recipient, long agentNumber) throws SQLException {
        assert relation == null || relation.isRoleType() : "The relation is either null or a role type.";
        
        final @Nonnull Role role = get(client, Roles.map(client, issuer, relation, recipient, agentNumber), issuer, relation, recipient, agentNumber);
        role.notify(CREATED);
        return role;
    }
    
    /**
     * Removes the given role from the database and index.
     * 
     * @param role the role to be removed.
     */
    private static void remove(@Nonnull Role role) throws SQLException {
        Roles.remove(role);
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Long, Role> map = index.get(role.getClient());
            if (map != null) map.remove(role.getNumber());
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param client the client that can assume the returned role.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nullable Role get(@Nonnull Client client, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final long number = resultSet.getLong(columnIndex);
        if (resultSet.wasNull()) return null;
        return Roles.load(client, number);
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param client the client that can assume the returned role.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Role getNotNull(@Nonnull Client client, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return Roles.load(client, resultSet.getLong(columnIndex));
    }
    
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + client.hashCode();
        hash = 41 * hash + (int) (number ^ (number >>> 32));
        return hash;
    }
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Role)) return false;
        final @Nonnull Role other = (Role) object;
        return this.client.equals(other.client) && this.number == other.number;
    }
    
}
