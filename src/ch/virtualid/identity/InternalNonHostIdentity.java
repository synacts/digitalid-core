package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an internal non-host identity.
 * 
 * @see Type
 * @see InternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface InternalNonHostIdentity extends InternalIdentity, NonHostIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code nonhost.internal@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("nonhost.internal@virtualid.ch").load(InternalIdentity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentifier getAddress();
    
}
