package net.digitalid.core.cryptography.signature.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.cryptography.credential.Credential;

/**
 * This exception is thrown when a credential is inactive.
 */
@Immutable
public class InactiveCredentialException extends InactiveAuthenticationException {
    
    /* -------------------------------------------------- Credential -------------------------------------------------- */
    
    /**
     * Stores the credential that is inactive.
     */
    private final @Nonnull Credential credential;
    
    /**
     * Returns the credential that is inactive.
     * 
     * @return the credential that is inactive.
     */
    @Pure
    public @Nonnull Credential getCredential() {
        return credential;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new inactive credential exception.
     * 
     * @param credential the credential that is inactive.
     */
    protected InactiveCredentialException(@Nonnull Credential credential) {
        this.credential = credential;
    }
    
    /**
     * Returns a new inactive credential exception.
     * 
     * @param credential the credential that is inactive.
     * 
     * @return a new inactive credential exception.
     */
    @Pure
    public static @Nonnull InactiveCredentialException get(@Nonnull Credential credential) {
        return new InactiveCredentialException(credential);
    }
    
}
