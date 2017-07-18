package net.digitalid.core.outgoingrole;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.credential.Credential;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.annotations.RoleType;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Node;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsBuilder;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;

/**
 * This class models an outgoing role that acts on behalf of an {@link Identity identity}.
 */
@Immutable
// TODO: @GenerateSubclass
@GenerateTableConverter
public abstract class OutgoingRole extends Agent {
    
    // TODO: Enable the issuance and revocation of outgoing roles here or somewhere else? If here, then make sure to pass the internal persons directly.
    
//    /* -------------------------------------------------- Issuance -------------------------------------------------- */
//    
//    /**
//     * Issues this outgoing role to the given contacts.
//     * 
//     * @param contacts the contacts to issue this outgoing role to.
//     * 
//     * @require isOnHost() : "This outgoing role is on a host.";
//     */
//    @NonCommitting
//    public void issue(@Nonnull ReadOnlySet<Contact> contacts) throws DatabaseException {
//        Require.that(isOnHost()).orThrow("This outgoing role is on a host.");
//        
//        for (final @Nonnull Contact contact : contacts) {
//            if (contact.isInternal()) {
//                Pusher.send(new OutgoingRoleIssue(this, contact.getInternalPerson()));
//            }
//        }
//    }
//    
//    /**
//     * Issues this outgoing role to the contacts of the context.
//     * 
//     * @require isOnHost() : "This outgoing role is on a host.";
//     */
//    @NonCommitting
//    public void issue() throws DatabaseException {
//        issue(getContext().getAllContacts());
//    }
//    
//    /* -------------------------------------------------- Revocation -------------------------------------------------- */
//    
//    /**
//     * Revokes this outgoing role from the given contacts.
//     * 
//     * @param contacts the contacts to revoke this outgoing role from.
//     * 
//     * @require isOnHost() : "This outgoing role is on a host.";
//     */
//    @NonCommitting
//    public void revoke(@Nonnull ReadOnlySet<Contact> contacts) throws DatabaseException {
//        Require.that(isOnHost()).orThrow("This outgoing role is on a host.");
//        
//        for (final @Nonnull Contact contact : contacts) {
//            if (contact.isInternal()) {
//                Pusher.send(new OutgoingRoleIssue(this, contact.getInternalPerson()));
//            }
//        }
//    }
//    
//    /**
//     * Revokes this outgoing role from the contacts of the context.
//     * 
//     * @require isOnHost() : "This outgoing role is on a host.";
//     */
//    @NonCommitting
//    public void revoke() throws DatabaseException {
//        revoke(getContext().getAllContacts());
//    }
//    
    /* -------------------------------------------------- Relation -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the relation.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, OutgoingRole, SemanticType> RELATION_AUTHORIZATION = RequiredAuthorizationBuilder.<NonHostEntity, Long, OutgoingRole, SemanticType>withRequiredPermissionsToExecuteMethod((concept, value) -> FreezableAgentPermissions.withPermission(value, true)).withRequiredAgentToExecuteMethod((concept, value) -> concept).withRequiredAgentToSeeMethod((concept, value) -> concept).build();
    
    /**
     * Returns the relation between the issuing and the receiving identity.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<OutgoingRole, @Nonnull @RoleType SemanticType> relation();
    
    @NonCommitting
    @PureWithSideEffects
    public void replaceRelation(@Nonnull SemanticType oldRelation, @Nonnull SemanticType newRelation) throws DatabaseException {
        // TODO: How to trigger the revocation and issuance with the property? With an observer?
        
//        if (isOnHost()) { revoke(); }
//        AgentModule.replaceRelation(this, oldRelation, newRelation);
//        relation = newRelation;
//        if (isOnHost()) { issue(); }
//        notify(RELATION);
    }
    
    /* -------------------------------------------------- Node -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the node.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, Long, OutgoingRole, Node> NODE_AUTHORIZATION = RequiredAuthorizationBuilder.<NonHostEntity, Long, OutgoingRole, Node>withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withNode(value).withWriteToNode(true).build()).withRequiredAgentToExecuteMethod((concept, value) -> concept).withRequiredAgentToSeeMethod((concept, value) -> concept).build();
    
    /**
     * Returns the node to which this outgoing role is assigned.
     */
    @Pure
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<OutgoingRole, @Nonnull /* TODO: @Matching (context.getEntity().equals(getEntity())) */ Node> node();
    
    @NonCommitting
    @PureWithSideEffects
    public void replaceContext(@Nonnull Node oldNode, @Nonnull Node newNode) throws DatabaseException {
        // TODO: How to trigger the revocation and issuance with the property? With an observer?
        
//        AgentModule.replaceContext(this, oldContext, newContext);
//        if (isOnHost()) {
//            final @Nonnull ReadOnlySet<Contact> oldContacts = oldContext.getAllContacts();
//            final @Nonnull ReadOnlySet<Contact> newContacts = newContext.getAllContacts();
//            revoke(oldContacts.subtract(newContacts));
//            issue(newContacts.subtract(oldContacts));
//        }
//        context = newContext;
//        notify(CONTEXT);
    }
    
    /* -------------------------------------------------- Restrictable -------------------------------------------------- */
    
    /**
     * Returns whether this outgoing role can be restricted.
     */
    @Pure
    @Default("false")
    public abstract boolean isRestrictable();
    
    /**
     * Restricts this outgoing role to the permissions and restrictions of the given credential.
     * 
     * @param credential the credential with which to restrict this outgoing role.
     * 
     * @require isRestrictable() : "This outgoing role can be restricted.";
     * @require credential.isRoleBased() : "The credential is role-based.";
     */
    @NonCommitting
    @PureWithSideEffects
    public void restrictTo(@Nonnull Credential credential) throws DatabaseException, RecoveryException {
        Require.that(isRestrictable()).orThrow("This outgoing role can be restricted.");
        Require.that(credential.isRoleBased()).orThrow("The credential is role-based.");
        
        permissions().get().clone().restrictTo(credential.getExposedPermissions());
        
        // TODO: How do we set the property to these permissions without persisting (and synchronizing) them?
        
//        if (permissions == null) { getPermissions(); }
//        assert permissions != null;
//        permissions.restrictTo(credential.getPermissionsNotNull());
        
        restrictions().get().restrictTo(credential.getProvidedRestrictions());
        
        // TODO: How do we set the property to these restrictions without persisting (and synchronizing) them?
        
//        if (restrictions == null) { getRestrictions(); }
//        assert restrictions != null;
//        restrictions = restrictions.restrictTo(credential.getRestrictionsNotNull());
    }
    
    /**
     * Checks whether this outgoing role covers the given credential and throws a {@link RequestException} if not.
     * 
     * @param credential the credential that needs to be covered.
     * 
     * @require credential.getPermissions() != null : "The permissions of the credential are not null.";
     * @require credential.getRestrictions() != null : "The restrictions of the credential are not null.";
     */
    @Pure
    @NonCommitting
    public void checkCovers(@Nonnull Credential credential) throws RequestException, DatabaseException, RecoveryException {
        final @Nullable ReadOnlyAgentPermissions permissions = credential.getPermissions();
        Require.that(permissions != null).orThrow("The permissions of the credential may not be null.");
        final @Nullable Restrictions restrictions = credential.getRestrictions();
        Require.that(restrictions != null).orThrow("The restrictions of the credential may not be null.");
        
        permissions().get().checkCover(permissions);
        restrictions().get().checkCover(restrictions);
    }
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    // TODO: Think about how to create concepts (and synchronize this across sites).
    
//    /**
//     * Creates a new outgoing role with the given context.
//     * 
//     * @param relation the relation between the issuing and the receiving identity.
//     * @param context the context to which the outgoing role is assigned.
//     * 
//     * @require relation.isRoleType() : "The relation is a role type.";
//     * @require context.isOnClient() : "The context is on a client.";
//     */
//    @Committing
//    public static @Nonnull OutgoingRole create(@Nonnull SemanticType relation, @Nonnull Context context) throws DatabaseException {
//        final @Nonnull OutgoingRole outgoingRole = get(context.getRole(), new Random().nextLong(), false, false);
//        Synchronizer.execute(new OutgoingRoleCreate(outgoingRole, relation, context));
//        return outgoingRole;
//    }
//    
//    /**
//     * Creates this outgoing role in the database.
//     * 
//     * @param relation the relation between the issuing and the receiving identity.
//     * @param context the context to which the outgoing role is assigned.
//     * 
//     * @require relation.isRoleType() : "The relation is a role type.";
//     * @require context.getEntity().equals(getEntity()) : "The context belongs to the entity of this outgoing role.";
//     */
//    @NonCommitting
//    @OnlyForActions
//    public void createForActions(@Nonnull SemanticType relation, @Nonnull Context context) throws DatabaseException {
//        AgentModule.addOutgoingRole(this, relation, context);
//        this.relation = relation;
//        this.context = context;
//        if (isOnHost()) { issue(); }
//        notify(Agent.CREATED);
//    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached outgoing role of the given entity that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull OutgoingRole of(@Nonnull NonHostEntity entity, long key) {
        return null;
//        return OutgoingRoleSubclass.MODULE.getConceptIndex().get(entity, key); // TODO
    }
    
}
