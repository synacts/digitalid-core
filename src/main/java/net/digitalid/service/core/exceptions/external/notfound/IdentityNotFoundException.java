package net.digitalid.service.core.exceptions.external.notfound;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when an {@link Identity identity} cannot be found.
 */
@Immutable
public class IdentityNotFoundException extends ExternalException {
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Stores the identifier that could not be resolved.
     */
    private final @Nonnull Identifier identifier;
    
    /**
     * Returns the identifier that could not be resolved.
     * 
     * @return the identifier that could not be resolved.
     */
    @Pure
    public final @Nonnull Identifier getIdentifier() {
        return identifier;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new identity not found exception with the given identifier.
     * 
     * @param identifier the identifier that could not be resolved.
     */
    protected IdentityNotFoundException(@Nonnull Identifier identifier) {
        super("The identity of " + identifier + " could not be found.");
        
        this.identifier = identifier;
    }
    
    /**
     * Returns a new identity not found exception with the given identifier.
     * 
     * @param identifier the identifier that could not be resolved.
     * 
     * @return a new identity not found exception with the given identifier.
     */
    @Pure
    public static @Nonnull IdentityNotFoundException get(@Nonnull Identifier identifier) {
        return new IdentityNotFoundException(identifier);
    }
    
}
