package net.digitalid.core.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.signature.host.HostSignature;

/**
 * This exception is thrown when a host signature has expired.
 */
@Immutable
public class ExpiredHostSignatureException extends ExpiredSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired host signature exception.
     * 
     * @param signature the host signature that has expired.
     */
    protected ExpiredHostSignatureException(@Nonnull HostSignature signature) {
        super(signature);
    }
    
    /**
     * Returns a new expired host signature exception.
     * 
     * @param signature the host signature that has expired.
     * 
     * @return a new expired host signature exception.
     */
    @Pure
    public static @Nonnull ExpiredHostSignatureException get(@Nonnull HostSignature signature) {
        return new ExpiredHostSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostSignature getSignature() {
        return (HostSignature) super.getSignature();
    }
    
}
