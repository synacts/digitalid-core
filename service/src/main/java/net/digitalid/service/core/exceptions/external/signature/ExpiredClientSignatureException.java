package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.signature.ClientSignatureWrapper;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This exception is thrown when a client signature has expired.
 */
@Immutable
public class ExpiredClientSignatureException extends ExpiredSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired client signature exception.
     * 
     * @param signature the client signature that has expired.
     */
    protected ExpiredClientSignatureException(@Nonnull ClientSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new expired client signature exception.
     * 
     * @param signature the client signature that has expired.
     * 
     * @return a new expired client signature exception.
     */
    @Pure
    public static @Nonnull ExpiredClientSignatureException get(@Nonnull ClientSignatureWrapper signature) {
        return new ExpiredClientSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ClientSignatureWrapper getSignature() {
        return (ClientSignatureWrapper) super.getSignature();
    }
    
}
