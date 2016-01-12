package net.digitalid.service.core.exceptions.external.signature;

import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.exceptions.external.ExternalException;

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
