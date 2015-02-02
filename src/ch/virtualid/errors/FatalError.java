package ch.virtualid.errors;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception is thrown when a fatal error occurs.
 * 
 * @see InitializationError
 * @see ShouldNeverHappenError
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class FatalError extends Error implements Immutable {
    
    /**
     * Stores the logger for fatal errors.
     */
    private static final @Nonnull Logger LOGGER = new Logger("Errors.log");
    
    
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
        
        LOGGER.log(Level.ERROR, "A fatal error occurred", this);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Logger.getMessage(this);
    }
    
}
