package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

/**
 * This exception is thrown when an element which may not be null is null.
 */
@Immutable
public class InvalidNullElementException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid null element exception.
     */
    protected InvalidNullElementException() {
        super("An element which may not be null is null.");
    }
    
    /**
     * Returns a new invalid null element exception.
     * 
     * @return a new invalid null element exception.
     */
    @Pure
    public static @Nonnull InvalidNullElementException get() {
        return new InvalidNullElementException();
    }
    
}
