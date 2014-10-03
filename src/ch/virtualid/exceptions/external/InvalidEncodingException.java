package ch.virtualid.exceptions.external;

import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when a block has an invalid encoding.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class InvalidEncodingException extends ExternalException implements Immutable {
    
    /**
     * Creates a new invalid encoding exception with the given message.
     * 
     * @param message a string explaining the exception that occurred.
     */
    public InvalidEncodingException(@Nonnull String message) {
        super(message);
    }
    
    /**
     * Creates a new invalid encoding exception with the given message and cause.
     * 
     * @param message a string explaining the exception that occurred.
     * @param cause a reference to the cause of the exception.
     */
    public InvalidEncodingException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
    
}
