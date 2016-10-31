package net.digitalid.core.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.signature.host.HostSignature;

/**
 * This exception is thrown when a host signature is invalid.
 */
@Immutable
public class InvalidHostSignatureException extends InvalidSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid host signature exception.
     * 
     * @param signature the host signature that is invalid.
     */
    protected InvalidHostSignatureException(@Nonnull HostSignature<?> signature) {
        super(signature);
    }
    
    /**
     * Returns a new invalid host signature exception.
     * 
     * @param signature the host signature that is invalid.
     * 
     * @return a new invalid host signature exception.
     */
    @Pure
    public static @Nonnull InvalidHostSignatureException get(@Nonnull HostSignature<?> signature) {
        return new InvalidHostSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostSignature<?> getSignature() {
        return (HostSignature<?>) super.getSignature();
    }
    
}
