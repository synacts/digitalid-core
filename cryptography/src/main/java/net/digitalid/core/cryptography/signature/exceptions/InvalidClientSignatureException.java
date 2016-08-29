package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.cryptography.signature.ClientSignature;

/**
 * This exception is thrown when a client signature is invalid.
 */
@Immutable
public class InvalidClientSignatureException extends InvalidSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid client signature exception.
     * 
     * @param signature the client signature that is invalid.
     */
    protected InvalidClientSignatureException(@Nonnull ClientSignature signature) {
        super(signature);
    }
    
    /**
     * Returns a new invalid client signature exception.
     * 
     * @param signature the client signature that is invalid.
     * 
     * @return a new invalid client signature exception.
     */
    @Pure
    public static @Nonnull InvalidClientSignatureException get(@Nonnull ClientSignature signature) {
        return new InvalidClientSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ClientSignature getSignature() {
        return (ClientSignature) super.getSignature();
    }
    
}
