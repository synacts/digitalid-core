package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;

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
