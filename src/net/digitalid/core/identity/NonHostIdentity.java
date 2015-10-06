package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.identifier.NonHostIdentifier;

/**
 * This interface models a non-host identity.
 * 
 * @see NonHostIdentityClass
 * @see InternalNonHostIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public interface NonHostIdentity extends Identity {
    
    /**
     * Stores the semantic type {@code nonhost@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.NONHOST_IDENTIFIER;
    
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
}
