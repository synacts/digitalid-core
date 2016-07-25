package net.digitalid.core.node;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.property.extensible.WritableExtensibleProperty;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.concept.annotations.GenerateProperty;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;
import net.digitalid.core.typeset.permissions.ReadOnlyNodePermissions;

/**
 * Description.
 */
@Immutable
// TODO: @GenerateConverter // TODO: How to provide a recover-method? Make it injectable?
public interface Node extends RootInterface {
    
    /* -------------------------------------------------- Number -------------------------------------------------- */
    
    /**
     * Returns the number that denotes this node.
     */
    @Pure
    public long getNumber();
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of this node.
     */
    @Pure
    @GenerateProperty
    public @Nonnull WritableExtensibleProperty<SemanticType, ReadOnlyNodePermissions> permissions();
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Returns the authentications of this node.
     */
    @Pure
    @NonCommitting
    public @Nonnull ReadOnlyAuthentications getAuthentications() throws DatabaseException; // TODO: Rather already a property?
    
    /* -------------------------------------------------- Subnodes -------------------------------------------------- */
    
    // TODO: Use the following code only in contexts.
    
//    /**
//     * Returns the direct subnodes of this node.
//     */
//    @Pure
//    @NonCommitting
//    public @Nonnull List<Node> getSubnodes() throws DatabaseException; // TODO: Rather already a property?
//    
//    /**
//     * Returns all subnodes of this node.
//     */
//    @Pure
//    @NonCommitting
//    public @Nonnull List<Node> getAllSubnodes() throws DatabaseException;
    
    /**
     * Returns whether this node is a supernode of the given node.
     */
    @Pure
    @NonCommitting
    public boolean isSupernodeOf(@Nonnull Node node) throws DatabaseException;
    
}
