package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.signature.HostSignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a host signature is invalid.
 */
@Immutable
public class InvalidHostSignatureException extends InvalidSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid host signature exception.
     * 
     * @param signature the host signature that is invalid.
     */
    protected InvalidHostSignatureException(@Nonnull HostSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new invalid host signature exception.
     * 
     * @param signature the host signature that is invalid.
     * 
     * @return a new invalid host signature exception.
     */
    @Pure
    public static @Nonnull InvalidHostSignatureException get(@Nonnull HostSignatureWrapper signature) {
        return new InvalidHostSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostSignatureWrapper getSignature() {
        return (HostSignatureWrapper) super.getSignature();
    }
    
}
