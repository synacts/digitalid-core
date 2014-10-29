package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an internal non-host identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface InternalNonHostIdentity extends InternalIdentity, Immutable {
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getInternalAddress();
    
}
