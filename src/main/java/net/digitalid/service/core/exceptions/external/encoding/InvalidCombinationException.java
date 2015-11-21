package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a combination of values is invalid.
 */
@Immutable
public class InvalidCombinationException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid combination exception with the given message.
     * 
     * @param message a string explaining which values cannot be combined.
     */
    protected InvalidCombinationException(@Nonnull String message) {
        super(message);
    }
    
    /**
     * Returns a new invalid combination exception with the given message.
     * 
     * @param message a string explaining which values cannot be combined.
     * 
     * @return a new invalid combination exception with the given message.
     */
    @Pure
    public static @Nonnull InvalidCombinationException get(@Nonnull String message) {
        return new InvalidCombinationException(message);
    }
    
}
