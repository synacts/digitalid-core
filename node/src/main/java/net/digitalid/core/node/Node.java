package net.digitalid.core.node;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.property.extensible.WritableExtensibleProperty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.concept.CoreConcept;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;
import net.digitalid.core.typeset.permissions.ReadOnlyNodePermissions;

/**
 * This class models a node, which is the superclass of contact and context.
 */
@Immutable
// TODO: @GenerateConverter // TODO: How to provide a recover-method? Make it injectable? (Use the key to determine whether it is a context or a contact (either with ranges or even vs. uneven)?)
public abstract class Node extends CoreConcept<NonHostEntity, Long> {
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    // TODO: Declare the required authorization object.
    
    /**
     * Returns the permissions of this node.
     */
    @Pure
//    @GenerateProperty(requiredPermissionsToExecuteMethod = "value, false", requiredRestrictionsToExecuteMethod = "false, false, true, concept", requiredPermissionsToSeeMethod = "value, false", requiredRestrictionsToSeeMethod = "false, false, false, concept")
//    @GenerateProperty(requiredPermissionsToExecuteMethod = "value, false", requiredRestrictionsToExecuteMethod = "RestrictionsBuilder.withWriteToNode(true).withNode(concept).build()", requiredPermissionsToSeeMethod = "value, false", requiredRestrictionsToSeeMethod = "RestrictionsBuilder.withNode(concept).build()")
    public abstract @Nonnull WritableExtensibleProperty<SemanticType, ReadOnlyNodePermissions> permissions();
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Returns the authentications of this node.
     */
    @Pure
//    @GenerateProperty(requiredPermissionsToExecuteMethod = "value, false", requiredRestrictionsToExecuteMethod = "false, false, true, concept", requiredPermissionsToSeeMethod = "value, false", requiredRestrictionsToSeeMethod = "false, false, false, concept")
    public abstract @Nonnull WritableExtensibleProperty<SemanticType, ReadOnlyAuthentications> getAuthentications();
    
    /* -------------------------------------------------- Supernode -------------------------------------------------- */
    
    /**
     * Returns whether this node is a supernode of the given node.
     */
    @Pure
    @NonCommitting
    public abstract boolean isSupernodeOf(@Nonnull Node node) throws DatabaseException;
    
}
