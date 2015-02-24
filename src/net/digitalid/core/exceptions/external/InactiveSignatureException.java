package net.digitalid.core.exceptions.external;

import javax.annotation.Nonnull;
import net.digitalid.core.interfaces.Immutable;

/**
 * This exception is thrown when a signature is inactive.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class InactiveSignatureException extends ExternalException implements Immutable {
    
    /**
     * Creates a new inactive signature exception with the given message.
     * 
     * @param message a string explaining the exception that occurred.
     */
    public InactiveSignatureException(@Nonnull String message) {
        super(message);
    }
    
}
