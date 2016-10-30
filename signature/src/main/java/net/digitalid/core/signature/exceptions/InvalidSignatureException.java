package net.digitalid.core.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.signature.Signature;

/**
 * This exception is thrown when a signature is invalid.
 * 
 * @see InvalidHostSignatureException
 * @see InvalidClientSignatureException
 * @see InvalidCredentialsSignatureException
 */
@Immutable
public abstract class InvalidSignatureException extends SignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid signature exception.
     * 
     * @param signature the signature that is invalid.
     */
    protected InvalidSignatureException(@Nonnull Signature<?> signature) {
        super(signature);
    }
    
}
