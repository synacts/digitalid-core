package net.digitalid.core.resolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.handler.Reply;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.NonHostIdentity;

import net.digitalid.service.core.block.wrappers.Blockable;

/**
 * This interface provides read-only access to {@link FreezablePredecessors predecessors} and should <em>never</em> be cast away.
 * 
 * @see FreezablePredecessors
 */
public interface ReadOnlyPredecessors extends ReadOnlyList<Predecessor>, Blockable {

    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezablePredecessors clone();
    
    /**
     * Returns the identities of the predecessors that are mapped.
     * 
     * @return the identities of the predecessors that are mapped.
     */
    @Pure
    @NonCommitting
    public @Nonnull @Frozen @NonNullableElements ReadOnlyList<NonHostIdentity> getIdentities() throws ExternalException;
    
    /**
     * Sets these values as the predecessors of the given identifier.
     * Only commit the transaction if the predecessors have been verified.
     * 
     * @param identifier the identifier whose predecessors are to be set.
     * @param reply the reply stating that the given identifier has these predecessors.
     */
    @Pure
    @NonCommitting
    public void set(@Nonnull InternalNonHostIdentifier identifier, @Nullable Reply reply) throws DatabaseException;
    
}
