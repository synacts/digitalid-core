package net.digitalid.core.identification.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identity.Identity;

/**
 * This exception is thrown when an {@link Identity identity} cannot be found.
 */
@Immutable
public class IdentityNotFoundException extends ExternalException {
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    private final @Nonnull Identifier identifier;
    
    /**
     * Returns the identifier that could not be resolved.
     */
    @Pure
    public @Nonnull Identifier getIdentifier() {
        return identifier;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new identity not found exception with the given identifier.
     * 
     * @param identifier the identifier that could not be resolved.
     */
    protected IdentityNotFoundException(@Nonnull Identifier identifier) {
        super("The identity of $ could not be found.", identifier.getString());
        
        this.identifier = identifier;
    }
    
    /**
     * Returns a new identity not found exception with the given identifier.
     * 
     * @param identifier the identifier that could not be resolved.
     */
    @Pure
    public static @Nonnull IdentityNotFoundException with(@Nonnull Identifier identifier) {
        return new IdentityNotFoundException(identifier);
    }
    
}
