package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This exception is thrown when a block has an invalid encoding.
 */
@Immutable
public abstract class InvalidEncodingException extends ExternalException {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new invalid encoding exception with the given message and cause.
     * 
     * @param message a string explaining the problem which has occurred.
     * @param cause the exception that caused this problem, if available.
     */
    protected InvalidEncodingException(@Nonnull String message, @Nullable Exception cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new invalid encoding exception with the given message.
     * 
     * @param message a string explaining the problem which has occurred.
     */
    protected InvalidEncodingException(@Nonnull String message) {
        super(message);
    }
    
}
