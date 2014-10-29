package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an internal identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface InternalIdentity extends Identity, Immutable {
    
    /**
     * Returns the address of this internal identity.
     * 
     * @return the address of this internal identity.
     */
    @Pure
    public @Nonnull InternalIdentifier getInternalAddress();
    
}
