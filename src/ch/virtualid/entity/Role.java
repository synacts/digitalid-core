package ch.virtualid.entity;

import ch.virtualid.agent.Agent;
import ch.virtualid.annotations.OnlyForActions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.service.Service;
import ch.virtualid.synchronizer.Synchronizer;
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
 * @see RoleModule
 * @see NativeRole
 * @see NonNativeRole
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Role extends EntityClass implements NonHostEntity, Immutable, SQLizable, Observer {
    
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
     * Creates a new role for the given client with the given number and issuer.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     */
    Role(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer) {
        this.client = client;
        this.number = number;
        this.issuer = issuer;
    }
    
    /**
     * Returns the client that can assume this role.
     * 
     * @return the client that can assume this role.
     */
    @Pure
    public final @Nonnull Client getClient() {
        return client;
    }
    
    /**
     * Returns the issuer of this role.
     * 
     * @return the issuer of this role.
     */
    @Pure
    public final @Nonnull InternalNonHostIdentity getIssuer() {
        return issuer;
    }
    
    
    /**
     * Returns the agent of this role.
     * 
     * @return the agent of this role.
     */
    @Pure
    public abstract @Nonnull Agent getAgent();
    
    
    /**
     * Returns whether this role is native.
     * 
     * @return whether this role is native.
     */
    @Pure
    public final boolean isNative() {
        return this instanceof NativeRole;
    }
    
    /**
     * Returns whether this role is not native.
     * 
     * @return whether this role is not native.
     */
    @Pure
    public final boolean isNotNative() {
        return this instanceof NonNativeRole;
    }
    
    /**
     * Returns this role as a native role.
     * 
     * @return this role as a native role.
     * 
     * @require isNative() : "This role is native.";
     */
    @Pure
    public final @Nonnull NativeRole toNativeRole() {
        assert isNative() : "This role is native.";
        
        return (NativeRole) this;
    }
    
    /**
     * Returns this role as a native role.
     * 
     * @return this role as a native role.
     * 
     * @require isNotNative() : "This role is not native.";
     */
    @Pure
    public final @Nonnull NonNativeRole toNonNativeRole() {
        assert isNotNative() : "This role is not native.";
        
        return (NonNativeRole) this;
    }
    
    
    @Pure
    @Override
    public final @Nonnull Client getSite() {
        return client;
    }
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentity getIdentity() {
        return issuer;
    }
    
    @Pure
    @Override
    public final long getNumber() {
        return number;
    }
    
    
    /**
     * Reloads the state of the given service for this role.
     * 
     * @param service the service whose state is to be reloaded for this role.
     */
    public final void reloadState(@Nonnull Service service) throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        Synchronizer.reload(this, service);
        getAgent().reset();
    }
    
    /**
     * Refreshes the state of the given service for this role.
     * 
     * @param service the service whose state is to be refreshed for this role.
     */
    public final void refreshState(@Nonnull Service service) throws SQLException, IOException, PacketException, ExternalException {
        Synchronizer.refresh(this, service);
        getAgent().reset();
    }
    
    
    /**
     * Stores the roles of this role.
     * 
     * @invariant roles == null || roles.isNotFrozen() : "The roles are not frozen.";
     * @invariant roles == null || roles.doesNotContainNull() : "The roles do not contain null.";
     * @invariant roles == null || roles.doesNotContainDuplicates() : "The roles do not contain duplicates.";
     */
    private @Nullable FreezableList<NonNativeRole> roles;
    
    /**
     * Returns the roles of this role.
     * 
     * @return the roles of this role.
     * 
     * @ensure return.isNotFrozen() : "The returned list is not frozen.";
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     * @ensure return.doesNotContainDuplicates() : "The returned list does not contain duplicates.";
     */
    @Pure
    public @Nonnull ReadonlyList<NonNativeRole> getRoles() throws SQLException {
        if (roles == null) roles = RoleModule.getRoles(this);
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
        
        final @Nonnull NonNativeRole role = NonNativeRole.add(client, issuer, relation, this, agentNumber);
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
    public abstract void remove() throws SQLException;
    
    
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
        return RoleModule.load(client, number);
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
        return RoleModule.load(client, resultSet.getLong(columnIndex));
    }
    
    
    @Pure
    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 41 * hash + client.hashCode();
        hash = 41 * hash + (int) (number ^ (number >>> 32));
        return hash;
    }
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Role)) return false;
        final @Nonnull Role other = (Role) object;
        return this.client.equals(other.client) && this.number == other.number;
    }
    
}
