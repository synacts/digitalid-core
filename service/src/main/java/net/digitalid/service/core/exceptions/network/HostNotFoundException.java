package net.digitalid.service.core.exceptions.network;

import java.net.UnknownHostException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

/**
 * This exception indicates that a host was not found.
 */
@Immutable
public class HostNotFoundException extends NetworkException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new host not found exception.
     * 
     * @param exception the original exception.
     * @param host the host that was not found.
     */
    protected HostNotFoundException(@Nonnull UnknownHostException exception, @Nonnull HostIdentifier host) {
        super(exception, host);
    }
    
    /**
     * Returns a new host not found exception.
     * 
     * @param exception the original exception.
     * @param host the host that was not found.
     * 
     * @return a new host not found exception.
     */
    @Pure
    public static @Nonnull HostNotFoundException get(@Nonnull UnknownHostException exception, @Nonnull HostIdentifier host) {
        return new HostNotFoundException(exception, host);
    }
    
}
