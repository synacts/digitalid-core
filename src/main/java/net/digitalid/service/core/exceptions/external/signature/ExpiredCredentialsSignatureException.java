package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.CredentialsSignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a credentials signature has expired.
 */
@Immutable
public class ExpiredCredentialsSignatureException extends ExpiredSignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired credentials signature exception.
     * 
     * @param signature the credentials signature that has expired.
     */
    protected ExpiredCredentialsSignatureException(@Nonnull CredentialsSignatureWrapper signature) {
        super(signature);
    }
    
    /**
     * Returns a new expired credentials signature exception.
     * 
     * @param signature the credentials signature that has expired.
     * 
     * @return a new expired credentials signature exception.
     */
    @Pure
    public static @Nonnull ExpiredCredentialsSignatureException get(@Nonnull CredentialsSignatureWrapper signature) {
        return new ExpiredCredentialsSignatureException(signature);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull CredentialsSignatureWrapper getSignature() {
        return (CredentialsSignatureWrapper) super.getSignature();
    }
    
}
