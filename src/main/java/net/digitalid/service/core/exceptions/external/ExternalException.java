package net.digitalid.service.core.exceptions.external;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.signature.InactiveAuthenticationException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.system.exceptions.CustomException;

/**
 * An external exception is caused by another party.
 * 
 * @see SignatureException
 * @see InvalidEncodingException
 * @see IdentityNotFoundException
 * @see SomethingNotFoundException
 * @see InvalidDeclarationException
 * @see InactiveAuthenticationException
 */
@Immutable
public abstract class ExternalException extends CustomException {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new external exception with the given message and cause.
     * 
     * @param message a string explaining the problem which has occurred.
     * @param cause the exception that caused this problem, if available.
     */
    protected ExternalException(@Nonnull String message, @Nullable Exception cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new external exception with the given message.
     * 
     * @param message a string explaining the problem which has occurred.
     */
    protected ExternalException(@Nonnull String message) {
        super(message);
    }
    
}
