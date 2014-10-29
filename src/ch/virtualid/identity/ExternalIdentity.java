package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an external identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ExternalIdentity extends Identity, Immutable {
    
    /**
     * Returns the address of this external identity.
     * 
     * @return the address of this external identity.
     */
    @Pure
    public @Nonnull ExternalIdentifier getExternalAddress();
    
}
