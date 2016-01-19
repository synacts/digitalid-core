package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;

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
    private final @Nonnull SignatureWrapper signature;
    
    /**
     * Returns the signature that has expired or is invalid.
     * 
     * @return the signature that has expired or is invalid.
     */
    @Pure
    public @Nonnull SignatureWrapper getSignature() {
        return signature;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new signature exception with the given signature.
     * 
     * @param signature the signature that has expired or is invalid.
     */
    protected SignatureException(@Nonnull SignatureWrapper signature) {
        super("A signature has expired or is invalid.");
        
        this.signature = signature;
    }
    
}
