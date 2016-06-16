package net.digitalid.core.conversion.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

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
     * Creates a new invalid null element exception with an error detail message.
     */
    protected InvalidNullElementException(@Nonnull String message) {
        super("An element which may not be null is null: " + message);
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
    
    /**
     * Returns a new invalid null element exception with an error detail message.
     */
    @Pure
    public static @Nonnull InvalidNullElementException get(@Nonnull String message) {
        return new InvalidNullElementException(message);
    }
    
}
