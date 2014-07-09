package ch.virtualid.identity;

import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown when an identity cannot be found.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class FailedIdentityException extends Exception implements Immutable {
    
    /**
     * Stores the identifier that could not be resolved.
     */
    private final @Nonnull Identifier identifier;
    
    /**
     * Creates a new identity not found exception with the given identifier.
     * 
     * @param identifier the identifier that could not be resolved.
     */
    public FailedIdentityException(@Nonnull Identifier identifier) {
        this(identifier, null);
    }
    
    /**
     * Creates a new identity not found exception with the given identifier and cause.
     * 
     * @param identifier the identifier that could not be resolved.
     * @param cause a reference to the cause of the problem.
     */
    public FailedIdentityException(@Nonnull Identifier identifier, @Nullable Exception cause) {
        super("The identity of " + identifier + " could not be established.", cause);
        this.identifier = identifier;
    }
    
    /**
     * Returns the identifier that could not be resolved.
     * 
     * @return the identifier that could not be resolved.
     */
    public @Nonnull Identifier getIdentifier() {
        return identifier;
    }
    
}
