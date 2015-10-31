package net.digitalid.service.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This exception is thrown when a block has an invalid encoding.
 */
@Immutable
public final class InvalidEncodingException extends ExternalException {
    
    /**
     * Creates a new invalid encoding exception with the given message.
     * 
     * @param message a string explaining the exception that occurred.
     */
    public InvalidEncodingException(@Nonnull String message) {
        super(message);
    }
    
    /**
     * Creates a new invalid encoding exception with the given cause.
     * 
     * @param cause a reference to the cause of the exception.
     */
    public InvalidEncodingException(@Nonnull Throwable cause) {
        super(cause);
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
