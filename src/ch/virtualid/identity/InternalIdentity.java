package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This interface models an internal identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface InternalIdentity extends Identity, Immutable {
    
    /**
     * Stores the semantic type {@code internal@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("internal@virtualid.ch").load(Identity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull InternalIdentifier getAddress();
    
}
