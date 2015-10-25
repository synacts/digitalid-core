package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.NonHostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This interface models a non-host identity.
 * 
 * @see NonHostIdentityClass
 * @see InternalNonHostIdentity
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
