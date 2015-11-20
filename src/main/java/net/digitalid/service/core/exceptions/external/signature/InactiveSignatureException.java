package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a signature is inactive.
 */
@Immutable
public class InactiveSignatureException extends ExternalException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new inactive signature exception with the given message.
     * 
     * @param message a string explaining the exception that occurred.
     */
    protected InactiveSignatureException(@Nonnull String message) {
        super(message);
    }
    
    /**
     * Creates a new inactive signature exception with the given message.
     * 
     * @param message a string explaining the exception that occurred.
     */
    @Pure
    public static @Nonnull InactiveSignatureException get(@Nonnull String message) {
        return new InactiveSignatureException(message);
    }
    
}
