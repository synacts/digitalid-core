package net.digitalid.core.packet.exceptions;

import java.net.UnknownHostException;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.exceptions.NetworkException;

import net.digitalid.service.core.identifier.HostIdentifier;

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
