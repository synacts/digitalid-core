package ch.virtualid.exceptions.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An external exception is caused by another party.
 * 
 * @see InvalidEncodingException
 * @see InvalidSignatureException
 * @see InvalidDeclarationException
 * 
 * @see IdentityNotFoundException
 * @see SomethingNotFoundException
 * 
 * @see InactiveSignatureException
 * @see ReplayDetectedException
 * 
 * @see WrongReplyException
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalException extends Exception implements Immutable {
    
    /**
     * Stores the logger for external exceptions.
     */
    private static final @Nonnull Logger logger = new Logger("Exceptions.log");
    
    
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
        
        logger.log(Level.WARNING, this);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Logger.getMessage(this);
    }
    
}
