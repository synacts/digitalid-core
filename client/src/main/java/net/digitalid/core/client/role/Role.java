package net.digitalid.core.client.role;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.client.Client;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.factories.RoleFactory;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;

/**
 * This class models a role on the client.
 * 
 * @see NativeRole
 * @see NonNativeRole
 */
@Immutable
@GenerateConverter
public abstract class Role extends RootClass implements NonHostEntity<Client> {
    
    /* -------------------------------------------------- Issuer -------------------------------------------------- */
    
    /**
     * Returns the issuer of this role.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull InternalNonHostIdentity getIssuer();
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity() {
        return getIssuer();
    }
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    /**
     * Returns the key of the agent.
     */
    @Pure
    @NonRepresentative
    public abstract long getAgentKey();
    
    /**
     * Returns the agent of this role.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull Agent getAgent();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull Role with(@Nonnull Client unit, long key) throws DatabaseException, RecoveryException {
        return RoleModule.load(unit, key);
    }
    
    /* -------------------------------------------------- Initializer -------------------------------------------------- */
    
    /**
     * Initializes the role factory.
     */
    @PureWithSideEffects
    @Initialize(target = RoleFactory.class)
    public static void initializeRoleFactory() {
        RoleFactory.configuration.set((client, key) -> Role.with((Client) client, key));
    }
    
    /* -------------------------------------------------- State -------------------------------------------------- */
    
    // TODO: Adapt the following code and maybe move it to some other location.
    
//    /**
//     * Reloads the state of the given module for this role.
//     * 
//     * @param module the module whose state is to be reloaded for this role.
//     */
//    @Committing
//    public final void reloadState(@Nonnull StateModule module) throws InterruptedException, ExternalException {
//        Synchronizer.reload(this, module);
//        if (Database.isMultiAccess() && (module.equals(CoreService.SERVICE) || module.equals(AgentModule.MODULE))) {
//            getAgent().reset();
//            this.roles = null;
//        }
//    }
//    
//    /**
//     * Refreshes the state of the given service for this role.
//     * 
//     * @param service the service whose state is to be refreshed for this role.
//     */
//    @Committing
//    public final void refreshState(@Nonnull Service service) throws InterruptedException, ExternalException {
//        Synchronizer.refresh(this, service);
//        if (Database.isMultiAccess() && service.equals(CoreService.SERVICE)) {
//            getAgent().reset();
//            this.roles = null;
//        }
//    }
//    
//    /**
//     * Reloads or refreshes the state of the given services for this role.
//     * 
//     * @param services the services whose state is to be reloaded or refreshed for this role.
//     * 
//     * @return whether this role is accredited for the given services.
//     */
//    @Committing
//    public boolean reloadOrRefreshState(@Nonnull Service... services) throws InterruptedException, ExternalException {
//        final @Nonnull Time[] times = new Time[services.length];
//        try {
//            Database.lock();
//            for (int i = 0; i < services.length; i++) {
//                times[i] = SynchronizerModule.getLastTime(this, services[i]);
//            }
//            Database.commit();
//        } catch (@Nonnull SQLException exception) {
//            Database.rollback();
//        } finally {
//            Database.unlock();
//        }
//        
//        final @Nonnull Time cutoff = Time.WEEK.ago();
//        for (int i = 0; i < services.length; i++) {
//            try {
//                if (times[i].isLessThan(cutoff)) { reloadState(services[i]); }
//                else { refreshState(services[i]); }
//            } catch (@Nonnull RequestException exception) {
//                if (exception.getCode() == RequestErrorCode.AUDIT) { return false; }
//                else { throw exception; }
//            }
//        }
//        return true;
//    }
//    
//    /**
//     * Waits until all actions of the given service are completed.
//     * 
//     * @param service the service whose actions are to be completed.
//     */
//    public final void waitForCompletion(@Nonnull Service service) throws InterruptedException {
//        SynchronizerModule.wait(this, service);
//    }
    
    /* -------------------------------------------------- Roles -------------------------------------------------- */
    
    // TODO: The supplied module in the generated subclass is not compatible with the first parameter of the following property.
    // TODO: Maybe re-implement the persistent property interface for roles that use the single role table.
    
//    /**
//     * Returns the non-native roles of this role.
//     */
//    @Pure
//    @GeneratePersistentProperty
//    public abstract @Nonnull WritablePersistentSimpleSetProperty<Role, NonNativeRole> roles();
    
    // TODO: Take care of the commented out observations:
    
//    /**
//     * Returns the roles of this role.
//     */
//    @Pure
//    @NonCommitting
//    public @Nonnull @NonFrozen @NonNullableElements @UniqueElements ReadOnlyList<NonNativeRole> getRoles() throws DatabaseException {
//        if (roles == null) { roles = RoleModule.getRoles(this); }
//        return roles;
//    }
//    
//    /**
//     * Adds the given role to the roles of this role.
//     * 
//     * @param issuer the issuer of the role to add.
//     * @param relation the relation of the role to add.
//     * @param agentNumber the agent number of the role to add.
//     * 
//     * @require relation.isRoleType() : "The relation is a role type.";
//     */
//    @NonCommitting
//    @OnlyForActions
//    public void addRole(@Nonnull InternalNonHostIdentity issuer, @Nonnull SemanticType relation, long agentNumber) throws DatabaseException {
//        Require.that(relation.isRoleType()).orThrow("The relation is a role type.");
//        
//        final @Nonnull NonNativeRole role = NonNativeRole.add(client, issuer, relation, this, agentNumber);
//        role.observe(this, DELETED);
//        
//        if (roles != null && !roles.contains(role)) { roles.add(role); }
//        notify(ADDED);
//    }
//    
//    @Override
//    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
//        if (aspect.equals(DELETED) && roles != null) { roles.remove(instance); }
//    }
//    
//    /**
//     * Removes this role.
//     */
//    @NonCommitting
//    @OnlyForActions
//    public void remove() throws DatabaseException {
//        RoleModule.remove(this);
//        ClientCredential.remove(this);
//        SynchronizerModule.remove(this);
//        notify(DELETED);
//    }
    
}
