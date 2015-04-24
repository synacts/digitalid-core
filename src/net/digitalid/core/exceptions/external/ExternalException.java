package net.digitalid.core.exceptions.external;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.interfaces.Immutable;

/**
 * An external exception is caused by another party.
 * 
 * @see InvalidEncodingException
 * @see InvalidSignatureException
 * @see InactiveSignatureException
 * @see InvalidDeclarationException
 * 
 * @see IdentityNotFoundException
 * @see SomethingNotFoundException
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class ExternalException extends Exception implements Immutable {
    
    /**
     * Creates a new external exception with the given message.
     * 
     * @param message a string explaining the exception.
     */
    protected ExternalException(@Nonnull String message) {
        this(message, null);
    }
    
    /**
     * Creates a new external exception with the given cause.
     * 
     * @param cause a reference to the cause of the exception.
     */
    protected ExternalException(@Nonnull Throwable cause) {
        this(null, cause);
    }
    
    /**
     * Creates a new external exception with the given message and cause.
     * 
     * @param message a string explaining the exception.
     * @param cause a reference to the cause of the exception.
     */
    protected ExternalException(@Nullable String message, @Nullable Throwable cause) {
        super(message == null ? "An external exception occurred." : message, cause);
        
//        Logger.log(Level.WARNING, "ExternalException", "An external exception occurred.", this);
    }
    
}
