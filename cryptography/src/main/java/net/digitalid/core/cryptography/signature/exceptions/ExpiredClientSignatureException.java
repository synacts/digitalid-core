package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.wrappers.signature.ClientSignatureWrapper;

/**
 * This exception is thrown when a client signature has expired.
 */
@Immutable
public class ExpiredClientSignatureException extends ExpiredSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired client signature exception.
     * 
     * @param signature the client signature that has expired.
     */
    protected ExpiredClientSignatureException(@Nonnull ClientSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new expired client signature exception.
     * 
     * @param signature the client signature that has expired.
     * 
     * @return a new expired client signature exception.
     */
    @Pure
    public static @Nonnull ExpiredClientSignatureException get(@Nonnull ClientSignatureWrapper signature) {
        return new ExpiredClientSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ClientSignatureWrapper getSignature() {
        return (ClientSignatureWrapper) super.getSignature();
    }
    
}
