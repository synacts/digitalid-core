package ch.virtualid.exceptions.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when an {@link Identity identity} cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class IdentityNotFoundException extends ExternalException implements Immutable {
    
    /**
     * Stores the identifier that could not be resolved.
     */
    private final @Nonnull Identifier identifier;
    
    /**
     * Creates a new identity not found exception with the given identifier.
     * 
     * @param identifier the identifier that could not be resolved.
     */
    public IdentityNotFoundException(@Nonnull Identifier identifier) {
        super("The identity of " + identifier + " could not be found.");
        
        this.identifier = identifier;
    }
    
    /**
     * Returns the identifier that could not be resolved.
     * 
     * @return the identifier that could not be resolved.
     */
    @Pure
    public @Nonnull Identifier getIdentifier() {
        return identifier;
    }
    
}
