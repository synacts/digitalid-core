package net.digitalid.core.restrictions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.set.WritablePersistentSetProperty;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.subject.CoreServiceCoreSubject;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;
import net.digitalid.core.typeset.authentications.ReadOnlyAuthentications;
import net.digitalid.core.typeset.permissions.FreezableNodePermissions;
import net.digitalid.core.typeset.permissions.ReadOnlyNodePermissions;

/**
 * This class models a node, which is the superclass of contact and context.
 */
@Immutable
@GenerateTableConverter(table = "unit_core_Node_Node") // TODO: How can we get the table name without adding the generated attribute table converter to the attribute core subject module?
public abstract class Node extends CoreServiceCoreSubject<NonHostEntity, Long> {
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of this node.
     */
    @Pure
    @GenerateSynchronizedProperty
    @Default("FreezableNodePermissions.withNoTypes()")
    public abstract @Nonnull WritablePersistentSetProperty<Node, SemanticType, ReadOnlyNodePermissions, FreezableNodePermissions> permissions();
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Returns the authentications of this node.
     */
    @Pure
    @GenerateSynchronizedProperty
    @Default("FreezableAuthentications.withNoTypes()")
    public abstract @Nonnull WritablePersistentSetProperty<Node, SemanticType, ReadOnlyAuthentications, FreezableAuthentications> authentications();
    
    /* -------------------------------------------------- Supernode -------------------------------------------------- */
    
    /**
     * Returns whether this node is a supernode of the given node.
     * This relation is reflexive (i.e. the method returns {@code true} for the same node).
     */
    @Pure
    @NonCommitting
    public abstract boolean isSupernodeOf(@Nonnull Node node) throws DatabaseException;
    
    // TODO: Include methods to aggregate the permissions and authentications over the contexts to which this node belongs.
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the node with the given key at the given entity.
     */
    @Pure
    @Recover
    public static @Nonnull Node of(@Nonnull NonHostEntity entity, long key) throws DatabaseException {
        return NodeFactory.configuration.get().getAgent(entity, key);
    }
    
}
