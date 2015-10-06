package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.identifier.InternalIdentifier;

/**
 * This interface models an internal identity.
 * 
 * @see HostIdentity
 * @see InternalNonHostIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public interface InternalIdentity extends Identity {
    
    /**
     * Stores the semantic type {@code internal@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("internal@core.digitalid.net").load(Identity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull InternalIdentifier getAddress();
    
}
