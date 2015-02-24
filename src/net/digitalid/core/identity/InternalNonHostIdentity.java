package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.interfaces.Immutable;

/**
 * This interface models an internal non-host identity.
 * 
 * @see Type
 * @see InternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface InternalNonHostIdentity extends InternalIdentity, NonHostIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code nonhost.internal@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("nonhost.internal@core.digitalid.net").load(InternalIdentity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentifier getAddress();
    
}
