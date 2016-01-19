package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;

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
    protected InvalidHostSignatureException(@Nonnull HostSignatureWrapper signature) {
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
    public static @Nonnull InvalidHostSignatureException get(@Nonnull HostSignatureWrapper signature) {
        return new InvalidHostSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostSignatureWrapper getSignature() {
        return (HostSignatureWrapper) super.getSignature();
    }
    
}
