package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface models an external identity.
 * 
 * @see ExternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface ExternalIdentity extends Identity, Immutable {
    
    /**
     * Stores the semantic type {@code external@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("external@virtualid.ch").load(Identity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull ExternalIdentifier getAddress();
    
    /**
     * Returns the successor of this external identity or null if there is none.
     * 
     * @return the successor of this external identity or null if there is none.
     */
    @Pure
    public @Nullable InternalIdentity getSuccessor();
    
}
