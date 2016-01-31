package net.digitalid.core.conversion.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.service.core.identifier.HostIdentifier;

/**
 * This exception is thrown when a method recipient does not run on this server.
 */
@Immutable
public class InvalidMethodRecipientException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Stores the recipient that does not run on this server.
     */
    private final @Nonnull HostIdentifier recipient;
    
    /**
     * Returns the recipient that does not run on this server.
     * 
     * @return the recipient that does not run on this server.
     */
    @Pure
    public final @Nonnull HostIdentifier getRecipient() {
        return recipient;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new method recipient exception.
     * 
     * @param recipient the recipient that does not run on this server.
     */
    protected InvalidMethodRecipientException(@Nonnull HostIdentifier recipient) {
        super(recipient + " does not run on this server.");
        
        this.recipient = recipient;
    }
    
    /**
     * Returns a new method recipient exception.
     * 
     * @param recipient the recipient that does not run on this server.
     * 
     * @return a new method recipient exception.
     */
    @Pure
    public static @Nonnull InvalidMethodRecipientException get(@Nonnull HostIdentifier recipient) {
        return new InvalidMethodRecipientException(recipient);
    }
    
}
