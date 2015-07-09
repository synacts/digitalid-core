package net.digitalid.core.errors;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;

/**
 * This error is thrown when an error occurs during initialization.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class InitializationError extends FatalError {
    
    /**
     * Creates a new initialization error with the given message.
     * 
     * @param message a string explaining the error that occurred.
     */
    public InitializationError(@Nonnull String message) {
        super(message, null);
    }
    
    /**
     * Creates a new initialization error with the given cause.
     * 
     * @param cause a reference to the cause of the error.
     */
    public InitializationError(@Nonnull Throwable cause) {
        super(null, cause);
    }
    
    /**
     * Creates a new initialization error with the given message and cause.
     * 
     * @param message a string explaining the error that occurred.
     * @param cause a reference to the cause of the error.
     */
    public InitializationError(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
    
}
