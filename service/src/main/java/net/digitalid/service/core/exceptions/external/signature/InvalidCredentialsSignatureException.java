package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This exception is thrown when a credentials signature is invalid.
 */
@Immutable
public class InvalidCredentialsSignatureException extends InvalidSignatureException {
    
    /* -------------------------------------------------- Parameter -------------------------------------------------- */
    
    /**
     * Stores the parameter name whose value is too big.
     */
    private final @Nullable String parameter;
    
    /**
     * Returns the parameter name whose value is too big.
     * 
     * @return the parameter name whose value is too big.
     */
    @Pure
    public final @Nullable String getParameter() {
        return parameter;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid credentials signature exception.
     * 
     * @param signature the credentials signature that is invalid.
     * @param parameter the parameter name whose value is too big.
     */
    protected InvalidCredentialsSignatureException(@Nonnull CredentialsSignatureWrapper signature, @Nullable String parameter) {
        super(signature);
        
        this.parameter = parameter;
    }
    
    /**
     * Returns a new invalid credentials signature exception.
     * 
     * @param signature the credentials signature that is invalid.
     * @param parameter the parameter name whose value is too big.
     * 
     * @return a new invalid credentials signature exception.
     */
    @Pure
    public static @Nonnull InvalidCredentialsSignatureException get(@Nonnull CredentialsSignatureWrapper signature, @Nullable String parameter) {
        return new InvalidCredentialsSignatureException(signature, parameter);
    }
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull CredentialsSignatureWrapper getSignature() {
        return (CredentialsSignatureWrapper) super.getSignature();
    }
    
}
