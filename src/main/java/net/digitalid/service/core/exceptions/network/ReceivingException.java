package net.digitalid.service.core.exceptions.network;

import java.io.IOException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception indicates that a response could not be received.
 */
@Immutable
public class ReceivingException extends NetworkException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new receiving exception.
     * 
     * @param exception the cause of the receiving exception.
     * @param host the host from which the response could not be received.
     */
    protected ReceivingException(@Nonnull IOException exception, @Nonnull HostIdentifier host) {
        super(exception, host);
    }
    
    /**
     * Returns a new receiving exception.
     * 
     * @param exception the cause of the receiving exception.
     * @param host the host from which the response could not be received.
     * 
     * @return a new receiving exception.
     */
    @Pure
    public static final @Nonnull ReceivingException get(@Nonnull IOException exception, @Nonnull HostIdentifier host) {
        return new ReceivingException(exception, host);
    }
    
}
