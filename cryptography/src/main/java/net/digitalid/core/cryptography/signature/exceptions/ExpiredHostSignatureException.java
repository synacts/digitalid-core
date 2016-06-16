package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;

/**
 * This exception is thrown when a host signature has expired.
 */
@Immutable
public class ExpiredHostSignatureException extends ExpiredSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired host signature exception.
     * 
     * @param signature the host signature that has expired.
     */
    protected ExpiredHostSignatureException(@Nonnull HostSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new expired host signature exception.
     * 
     * @param signature the host signature that has expired.
     * 
     * @return a new expired host signature exception.
     */
    @Pure
    public static @Nonnull ExpiredHostSignatureException get(@Nonnull HostSignatureWrapper signature) {
        return new ExpiredHostSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostSignatureWrapper getSignature() {
        return (HostSignatureWrapper) super.getSignature();
    }
    
}
