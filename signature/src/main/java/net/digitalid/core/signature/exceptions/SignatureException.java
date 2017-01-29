package net.digitalid.core.signature.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.signature.Signature;

/**
 * This exception is thrown when a signature has expired or is invalid.
 * 
 * @see ExpiredSignatureException
 * @see InvalidSignatureException
 * @see InactiveSignatureException
 */
@Immutable
public abstract class SignatureException extends ExternalException {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Returns the signature that has expired or is invalid.
     */
    @Pure
    public abstract @Nonnull Signature<?> getSignature();
    
    /* -------------------------------------------------- Message -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getMessage() {
        return "A signature has expired or is invalid.";
    }
    
    /* -------------------------------------------------- Cause -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable Throwable getCause() {
        return null;
    }
    
}
