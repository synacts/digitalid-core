package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This exception is thrown when a signature has expired.
 * 
 * @see ExpiredHostSignatureException
 * @see ExpiredClientSignatureException
 * @see ExpiredCredentialsSignatureException
 */
@Immutable
public abstract class ExpiredSignatureException extends SignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new expired signature exception.
     * 
     * @param signature the signature that has expired.
     */
    protected ExpiredSignatureException(@Nonnull SignatureWrapper signature) {
        super(signature);
    }
    
}
