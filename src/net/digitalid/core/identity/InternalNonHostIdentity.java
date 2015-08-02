package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.InternalNonHostIdentifier;

/**
 * This interface models an internal non-host identity.
 * 
 * @see Type
 * @see InternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
