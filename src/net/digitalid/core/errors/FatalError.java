package net.digitalid.core.errors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;

/**
 * This exception is thrown when a fatal error occurs.
 * 
 * @see InitializationError
 * @see ShouldNeverHappenError
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class FatalError extends Error {
    
    /**
     * Creates a new fatal error with the given message.
     * 
     * @param message a string explaining the error.
     */
    protected FatalError(@Nonnull String message) {
        this(message, null);
    }
    
    /**
     * Creates a new fatal error with the given cause.
     * 
     * @param cause a reference to the cause of the error.
     */
    protected FatalError(@Nonnull Throwable cause) {
        this(null, cause);
    }
    
    /**
     * Creates a new fatal error with the given message and cause.
     * 
     * @param message a string explaining the error.
     * @param cause a reference to the cause of the error.
     */
    protected FatalError(@Nullable String message, @Nullable Throwable cause) {
        super(message == null ? "A fatal error occurred." : message, cause);
        
        Logger.log(Level.ERROR, "FatalError", "A fatal error occurred.", this);
    }
    
}
