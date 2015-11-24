package net.digitalid.service.core.exceptions.network;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception indicates that a packet could not be sent.
 */
@Immutable
public class SendingException extends NetworkException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new sending exception.
     * 
     * @param exception the cause of the sending exception.
     * @param host the host to which the request could not be sent.
     */
    protected SendingException(@Nonnull IOException exception, @Nullable HostIdentifier host) {
        super(exception, host);
    }
    
    /**
     * Returns a new sending exception.
     * 
     * @param exception the cause of the sending exception.
     * @param host the host to which the request could not be sent.
     * 
     * @return a new sending exception.
     */
    @Pure
    public static final @Nonnull SendingException get(@Nonnull IOException exception, @Nullable HostIdentifier host) {
        return new SendingException(exception, host);
    }
    
}
