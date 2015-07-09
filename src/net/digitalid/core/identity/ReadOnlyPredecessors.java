package net.digitalid.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Reply;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.interfaces.Blockable;

/**
 * This interface provides read-only access to {@link Predecessors predecessors} and should <em>never</em> be cast away.
 * 
 * @see Predecessors
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface ReadOnlyPredecessors extends ReadOnlyList<Predecessor>, Blockable {

    @Pure
    @Override
    public @Capturable @Nonnull Predecessors clone();
    
    /**
     * Returns the identities of the predecessors that are mapped.
     * 
     * @return the identities of the predecessors that are mapped.
     * 
     * @ensure return.isFrozen() : "The returned list is frozen.";
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     */
    @NonCommitting
    public @Nonnull ReadOnlyList<NonHostIdentity> getIdentities() throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Sets these values as the predecessors of the given identifier.
     * Only commit the transaction if the predecessors have been verified.
     * 
     * @param identifier the identifier whose predecessors are to be set.
     * @param reply the reply stating that the given identifier has these predecessors.
     */
    @NonCommitting
    public void set(@Nonnull InternalNonHostIdentifier identifier, @Nullable Reply reply) throws SQLException;
    
}
