package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This exception is thrown when a signature is invalid.
 */
@Immutable
public final class InvalidSignatureException extends ExternalException {
    
    /**
     * Creates a new invalid signature exception with the given message.
     * 
     * @param message a string explaining the exception that occurred.
     */
    public InvalidSignatureException(@Nonnull String message) {
        super(message);
    }
    
}
