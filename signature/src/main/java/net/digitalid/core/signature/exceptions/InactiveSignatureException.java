package net.digitalid.core.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.signature.Signature;

/**
 * This exception is thrown when a signature is inactive.
 */
@Immutable
public class InactiveSignatureException extends InactiveAuthenticationException {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Stores the signature that is inactive.
     */
    private final @Nonnull Signature signature;
    
    /**
     * Returns the signature that is inactive.
     * 
     * @return the signature that is inactive.
     */
    @Pure
    public @Nonnull Signature getSignature() {
        return signature;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new inactive signature exception.
     * 
     * @param signature the signature that is inactive.
     */
    protected InactiveSignatureException(@Nonnull Signature signature) {
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
    public static @Nonnull InactiveSignatureException get(@Nonnull Signature signature) {
        return new InactiveSignatureException(signature);
    }
    
}
