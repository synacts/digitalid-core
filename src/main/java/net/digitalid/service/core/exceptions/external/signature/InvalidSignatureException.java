package net.digitalid.service.core.exceptions.external.signature;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * This exception is thrown when a signature is invalid.
 * 
 * @see InvalidHostSignatureException
 * @see InvalidClientSignatureException
 * @see InvalidCredentialsSignatureException
 */
@Immutable
public abstract class InvalidSignatureException extends SignatureException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid signature exception.
     * 
     * @param signature the signature that is invalid.
     */
    protected InvalidSignatureException(@Nonnull SignatureWrapper signature) {
        super(signature);
    }
    
}
