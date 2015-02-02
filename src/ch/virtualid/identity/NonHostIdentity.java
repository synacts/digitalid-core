package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models a non-host identity.
 * 
 * @see NonHostIdentityClass
 * @see InternalNonHostIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface NonHostIdentity extends Identity, Immutable {
    
    /**
     * Stores the semantic type {@code nonhost@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.NONHOST_IDENTIFIER;
    
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
}
