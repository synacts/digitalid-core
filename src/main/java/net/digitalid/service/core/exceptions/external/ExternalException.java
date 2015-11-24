package net.digitalid.service.core.exceptions.external;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.signature.InactiveAuthenticationException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.system.logger.Log;

/**
 * An external exception is caused by another party.
 * 
 * @see SignatureException
 * @see InvalidEncodingException
 * @see InvalidDeclarationException
 * @see InactiveAuthenticationException
 * 
 * @see IdentityNotFoundException
 * @see SomethingNotFoundException
 */
@Immutable
public abstract class ExternalException extends Exception {
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new external exception with the given message and cause.
     * 
     * @param message a string explaining the problem which has occurred.
     * @param cause the exception that caused this problem, if available.
     */
    protected ExternalException(@Nonnull String message, @Nullable Exception cause) {
        super(message, cause);
        
        Log.warning("An external exception occurred.", this);
    }
    
    /**
     * Creates a new external exception with the given message.
     * 
     * @param message a string explaining the problem which has occurred.
     */
    protected ExternalException(@Nonnull String message) {
        this(message, null);
    }
    
}
