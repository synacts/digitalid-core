package net.digitalid.service.core.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.action.synchronizer.Synchronizer;
import net.digitalid.service.core.action.synchronizer.SynchronizerModule;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.AgentModule;
import net.digitalid.service.core.cryptography.credential.ClientCredential;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.elements.UniqueElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.NonLocked;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a role on the client-side.
 * 
 * @see RoleModule
 * @see NativeRole
 * @see NonNativeRole
 */
@Immutable
public abstract class Role extends EntityImplementation implements NonHostEntity, Observer {
    
    /**
     * Stores the aspect of a new role being added to the observed role.
     */
    public static final @Nonnull Aspect ADDED = new Aspect(Role.class, "added");
    
    
    /**
     * Stores the semantic type {@code issuer.role@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ISSUER = SemanticType.map("issuer.role@core.digitalid.net").load(InternalNonHostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code relation.role@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType RELATION = SemanticType.map("relation.role@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code agent.role@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType AGENT = SemanticType.map("agent.role@core.digitalid.net").load(Agent.NUMBER);
    
    
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
     * Returns whether this role is non-native.
     * 
     * @return whether this role is non-native.
     */
    @Pure
    public final boolean isNonNative() {
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
     * Returns this role as a non-native role.
     * 
     * @return this role as a non-native role.
     * 
     * @require isNonNative() : "This role is non-native.";
     */
    @Pure
    public final @Nonnull NonNativeRole toNonNativeRole() {
        assert isNonNative() : "This role is non-native.";
        
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
     * Reloads the state of the given module for this role.
     * 
     * @param module the module whose state is to be reloaded for this role.
     */
    @NonLocked
    @Committing
    public final void reloadState(@Nonnull StateModule module) throws InterruptedException, AbortException, PacketException, ExternalException, NetworkException {
        Synchronizer.reload(this, module);
        if (Database.isMultiAccess() && (module.equals(CoreService.SERVICE) || module.equals(AgentModule.MODULE))) {
            getAgent().reset();
            this.roles = null;
        }
    }
    
    /**
     * Refreshes the state of the given service for this role.
     * 
     * @param service the service whose state is to be refreshed for this role.
     */
    @NonLocked
    @Committing
    public final void refreshState(@Nonnull Service service) throws InterruptedException, AbortException, PacketException, ExternalException, NetworkException {
        Synchronizer.refresh(this, service);
        if (Database.isMultiAccess() && service.equals(CoreService.SERVICE)) {
            getAgent().reset();
            this.roles = null;
        }
    }
    
    /**
     * Reloads or refreshes the state of the given services for this role.
     * 
     * @param services the services whose state is to be reloaded or refreshed for this role.
     * 
     * @return whether this role is accredited for the given services.
     */
    @NonLocked
    @Committing
    public boolean reloadOrRefreshState(@Nonnull Service... services) throws InterruptedException, AbortException, PacketException, ExternalException, NetworkException {
        final @Nonnull Time[] times = new Time[services.length];
        try {
            Database.lock();
            for (int i = 0; i < services.length; i++) {
                times[i] = SynchronizerModule.getLastTime(this, services[i]);
            }
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            Database.rollback();
        } finally {
            Database.unlock();
        }
        
        final @Nonnull Time cutoff = Time.WEEK.ago();
        for (int i = 0; i < services.length; i++) {
            try {
                if (times[i].isLessThan(cutoff)) reloadState(services[i]);
                else refreshState(services[i]);
            } catch (@Nonnull PacketException exception) {
                if (exception.getError() == PacketErrorCode.AUDIT) return false;
                else throw exception;
            }
        }
        return true;
    }
    
    
    /**
     * Waits until all actions of the given service are completed.
     * 
     * @param service the service whose actions are to be completed.
     */
    public final void waitForCompletion(@Nonnull Service service) throws InterruptedException {
        SynchronizerModule.wait(this, service);
    }
    
    
    /**
     * Stores the roles of this role.
     */
    private @Nullable @NonFrozen @NonNullableElements @UniqueElements FreezableList<NonNativeRole> roles;
    
    /**
     * Returns the roles of this role.
     * 
     * @return the roles of this role.
     */
    @Pure
    @NonCommitting
    public @Nonnull @NonFrozen @NonNullableElements @UniqueElements ReadOnlyList<NonNativeRole> getRoles() throws AbortException {
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
    @NonCommitting
    @OnlyForActions
    public void addRole(@Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, long agentNumber) throws AbortException {
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
    @NonCommitting
    @OnlyForActions
    public void remove() throws AbortException {
        RoleModule.remove(this);
        ClientCredential.remove(this);
        SynchronizerModule.remove(this);
        notify(DELETED);
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
    @NonCommitting
    public static @Nullable Role get(@Nonnull Client client, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws AbortException {
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
    @NonCommitting
    public static @Nonnull Role getNotNull(@Nonnull Client client, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws AbortException {
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
