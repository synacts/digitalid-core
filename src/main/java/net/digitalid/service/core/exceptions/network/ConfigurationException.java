package net.digitalid.service.core.exceptions.network;

import java.net.SocketException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception indicates that a configuration did not work.
 */
@Immutable
public class ConfigurationException extends NetworkException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new configuration exception.
     * 
     * @param exception the cause of the configuration exception.
     * @param host the host whose socket could not be configured.
     */
    protected ConfigurationException(@Nonnull SocketException exception, @Nonnull HostIdentifier host) {
        super(exception, host);
    }
    
    /**
     * Returns a new configuration exception.
     * 
     * @param exception the cause of the configuration exception.
     * @param host the host whose socket could not be configured.
     * 
     * @return a new configuration exception.
     */
    @Pure
    public static final @Nonnull ConfigurationException get(@Nonnull SocketException exception, @Nonnull HostIdentifier host) {
        return new ConfigurationException(exception, host);
    }
    
}
