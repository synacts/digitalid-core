package net.digitalid.service.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This exception is thrown when a block has an invalid encoding.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
     * Creates a new invalid encoding exception with the given message and cause.
     * 
     * @param message a string explaining the exception that occurred.
     * @param cause a reference to the cause of the exception.
     */
    public InvalidEncodingException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
    
}
