package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This interface models an internal non-host identity.
 * 
 * @see Type
 * @see InternalPerson
 */
@Immutable
public interface InternalNonHostIdentity extends InternalIdentity, NonHostIdentity {
    
    /**
     * Stores the semantic type {@code nonhost.internal@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("nonhost.internal@core.digitalid.net").load(InternalIdentity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentifier getAddress();
    
}
