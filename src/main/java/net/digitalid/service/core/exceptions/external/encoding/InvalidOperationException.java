package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when the operation that should have been executed is invalid.
 */
@Immutable
public class InvalidOperationException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid operation exception with the given message.
     * 
     * @param message a string explaining the problem which has occurred.
     */
    protected InvalidOperationException(@Nonnull String message) {
        super(message);
    }
    
    /**
     * Returns a new invalid operation exception with the given message.
     * 
     * @param message a string explaining the problem which has occurred.
     * 
     * @return a new invalid operation exception with the given message.
     */
    @Pure
    public static @Nonnull InvalidOperationException get(@Nonnull String message) {
        return new InvalidOperationException(message);
    }
    
}
