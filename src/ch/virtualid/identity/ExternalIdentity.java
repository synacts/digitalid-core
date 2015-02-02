package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an external identity.
 * 
 * @see ExternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface ExternalIdentity extends Identity, Immutable {
    
    /**
     * Stores the semantic type {@code external@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("external@virtualid.ch").load(Identity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
}
