package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a block which may not be null is null.
 */
@Immutable
public class InvalidNullException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid null exception.
     */
    protected InvalidNullException() {
        super("A block which may not be null is null.");
    }
    
    /**
     * Returns a new invalid null exception.
     * 
     * @return a new invalid null exception.
     */
    @Pure
    public static @Nonnull InvalidNullException get() {
        return new InvalidNullException();
    }
    
}
