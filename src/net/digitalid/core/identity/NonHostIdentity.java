package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.NonHostIdentifier;
import net.digitalid.core.interfaces.Immutable;

/**
 * This interface models a non-host identity.
 * 
 * @see NonHostIdentityClass
 * @see InternalNonHostIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface NonHostIdentity extends Identity, Immutable {
    
    /**
     * Stores the semantic type {@code nonhost@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.NONHOST_IDENTIFIER;
    
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
}
