package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This exception is thrown when a signature is inactive.
 */
@Immutable
public class InactiveSignatureException extends InactiveAuthenticationException {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Stores the signature that is inactive.
     */
    private final @Nonnull SignatureWrapper signature;
    
    /**
     * Returns the signature that is inactive.
     * 
     * @return the signature that is inactive.
     */
    @Pure
    public @Nonnull SignatureWrapper getSignature() {
        return signature;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new inactive signature exception.
     * 
     * @param signature the signature that is inactive.
     */
    protected InactiveSignatureException(@Nonnull SignatureWrapper signature) {
        this.signature = signature;
    }
    
    /**
     * Returns a new inactive signature exception.
     * 
     * @param signature the signature that is inactive.
     * 
     * @return a new inactive signature exception.
     */
    @Pure
    public static @Nonnull InactiveSignatureException get(@Nonnull SignatureWrapper signature) {
        return new InactiveSignatureException(signature);
    }
    
}
