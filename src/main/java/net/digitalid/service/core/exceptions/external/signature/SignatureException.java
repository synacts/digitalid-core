package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

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
