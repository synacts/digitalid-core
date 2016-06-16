package net.digitalid.core.exceptions;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.identifier.HostIdentifier;

/**
 * This exception indicates an error on the network layer.
 * 
 * @see ConfigurationException
 * @see HostNotFoundException
 * @see ReceivingException
 * @see SendingException
 */
@Immutable
public abstract class NetworkException extends ExternalException {
    
    /* -------------------------------------------------- Host -------------------------------------------------- */
    
    /**
     * Stores the host with which the communication failed.
     */
    private final @Nonnull HostIdentifier host;
    
    /**
     * Returns the host with which the communication failed.
     * 
     * @return the host with which the communication failed.
     */
    @Pure
    public final @Nonnull HostIdentifier getHost() {
        return host;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new network exception.
     * 
     * @param exception the cause of the network exception.
     * @param host the host with which the communication failed.
     */
    protected NetworkException(@Nonnull IOException exception, @Nonnull HostIdentifier host) {
        super("A network exception occurred during the communication with " + host + ".", exception);
        
        this.host = host;
    }
    
    /* -------------------------------------------------- Cause -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull IOException getCause() {
        return (IOException) super.getCause();
    }
    
}