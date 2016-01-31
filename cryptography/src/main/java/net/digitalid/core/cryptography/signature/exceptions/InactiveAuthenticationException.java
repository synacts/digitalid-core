package net.digitalid.core.cryptography.signature.exceptions;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception is thrown when an authentication is inactive.
 * 
 * @see InactiveCredentialException
 * @see InactiveSignatureException
 */
@Immutable
public abstract class InactiveAuthenticationException extends ExternalException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new inactive authentication exception.
     */
    protected InactiveAuthenticationException() {
        super("An authentication is inactive.");
    }
    
}
