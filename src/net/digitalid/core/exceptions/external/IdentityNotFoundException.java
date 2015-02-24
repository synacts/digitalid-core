package net.digitalid.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.interfaces.Immutable;

/**
 * This exception is thrown when an {@link Identity identity} cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
