package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.cryptography.signature.Signature;

/**
 * This exception is thrown when a signature has expired or is invalid.
 * 
 * @see ExpiredSignatureException
 * @see InvalidSignatureException
 */
@Immutable
public abstract class SignatureException extends ExternalException {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Stores the signature that has expired or is invalid.
     */
    private final @Nonnull Signature<?> signature;
    
    /**
     * Returns the signature that has expired or is invalid.
     * 
     * @return the signature that has expired or is invalid.
     */
    @Pure
    public @Nonnull Signature<?> getSignature() {
        return signature;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new signature exception with the given signature.
     * 
     * @param signature the signature that has expired or is invalid.
     */
    protected SignatureException(@Nonnull Signature<?> signature) {
        super("A signature has expired or is invalid.");
        
        this.signature = signature;
    }
    
}
