package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;

/**
 * This exception is thrown when a signature has expired.
 * 
 * @see ExpiredHostSignatureException
 * @see ExpiredClientSignatureException
 * @see ExpiredCredentialsSignatureException
 */
@Immutable
public abstract class ExpiredSignatureException extends SignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired signature exception.
     * 
     * @param signature the signature that has expired.
     */
    protected ExpiredSignatureException(@Nonnull SignatureWrapper signature) {
        super(signature);
    }
    
}
