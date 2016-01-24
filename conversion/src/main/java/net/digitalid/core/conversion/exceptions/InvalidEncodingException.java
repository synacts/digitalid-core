package net.digitalid.core.conversion.exceptions;

import net.digitalid.utility.exceptions.ExternalException;

/**
 * This exception is thrown when a block has an invalid encoding.
 */
public abstract class InvalidEncodingException extends ExternalException {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new invalid encoding exception with the given message and cause.
     * 
     * @param message a string explaining the problem which has occurred.
     * @param cause the exception that caused this problem, if available.
     */
    protected InvalidEncodingException(String message, Exception cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new invalid encoding exception with the given message.
     * 
     * @param message a string explaining the problem which has occurred.
     */
    protected InvalidEncodingException(String message) {
        super(message);
    }
    
}
