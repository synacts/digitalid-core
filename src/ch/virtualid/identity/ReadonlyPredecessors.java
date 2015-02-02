package ch.virtualid.identity;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Reply;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface provides readonly access to {@link Predecessors predecessors} and should <em>never</em> be cast away.
 * 
 * @see Predecessors
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ReadonlyPredecessors extends ReadonlyList<Predecessor>, Blockable {

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
    @DoesNotCommit
    public @Nonnull ReadonlyList<NonHostIdentity> getIdentities() throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Sets these values as the predecessors of the given identifier.
     * Only commit the transaction if the predecessors have been verified.
     * 
     * @param identifier the identifier whose predecessors are to be set.
     * @param reply the reply stating that the given identifier has these predecessors.
     */
    @DoesNotCommit
    public void set(@Nonnull InternalNonHostIdentifier identifier, @Nullable Reply reply) throws SQLException;
    
}
