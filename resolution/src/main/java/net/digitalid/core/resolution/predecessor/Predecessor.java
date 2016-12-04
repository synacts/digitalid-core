package net.digitalid.core.resolution.predecessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identifier.NonHostIdentifier;
import net.digitalid.core.identification.identity.NonHostIdentity;

/**
 * This class models a predecessor of an identifier.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
// TODO: @GenerateConverter
public abstract class Predecessor extends RootClass {
    
    // TOOD: Make sure that cyclic semantic types are still possible.
    
//    /**
//     * Stores the semantic type {@code predecessor.identity@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType TYPE = SemanticType.map("predecessor.identity@core.digitalid.net");
//    
//    /**
//     * Stores the semantic type {@code list.predecessor.identity@core.digitalid.net}.
//     */
//    static final @Nonnull SemanticType PREDECESSORS = SemanticType.map("list.predecessor.identity@core.digitalid.net").load(ListWrapper.XDF_TYPE, TYPE);
//    
//    // Load the recursive declaration of the predecessor type.
//    static { TYPE.load(TupleWrapper.XDF_TYPE, NonHostIdentity.IDENTIFIER, PREDECESSORS); }
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this predecessor.
     */
    @Pure
    public abstract @Nonnull NonHostIdentifier getIdentifier();
    
    /* -------------------------------------------------- Predecessors -------------------------------------------------- */
    
    /**
     * Returns the predecessors of this predecessor.
     */
    @Pure
    // TODO: @Default("identifier instanceof InternalNonHostIdentifier ? FreezablePredecessors.get((InternalNonHostIdentifier) identifier) : new FreezablePredecessors().freeze()")
    public abstract @Nonnull @Frozen ReadOnlyPredecessors getPredecessors();
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Returns the identity of this predecessor or null if none of its predecessors (including itself) is mapped.
     */
    @Pure
    @NonCommitting
    @Nullable NonHostIdentity getIdentity() throws ExternalException {
        // TODO: if (getIdentifier().isMapped()) { return getIdentifier().getMappedIdentity(); }
        if (!getPredecessors().getIdentities().isEmpty()) { return getIdentifier().resolve().castTo(NonHostIdentity.class); }
        return null;
    }
    
}
