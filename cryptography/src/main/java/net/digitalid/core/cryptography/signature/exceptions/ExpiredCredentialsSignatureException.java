package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;

/**
 * This exception is thrown when a credentials signature has expired.
 */
@Immutable
public class ExpiredCredentialsSignatureException extends ExpiredSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired credentials signature exception.
     * 
     * @param signature the credentials signature that has expired.
     */
    protected ExpiredCredentialsSignatureException(@Nonnull CredentialsSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new expired credentials signature exception.
     * 
     * @param signature the credentials signature that has expired.
     * 
     * @return a new expired credentials signature exception.
     */
    @Pure
    public static @Nonnull ExpiredCredentialsSignatureException get(@Nonnull CredentialsSignatureWrapper signature) {
        return new ExpiredCredentialsSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull CredentialsSignatureWrapper getSignature() {
        return (CredentialsSignatureWrapper) super.getSignature();
    }
    
}
