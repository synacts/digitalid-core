package net.digitalid.service.core.exceptions.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception indicates an error on the network layer, such as a connection or socket timeout.
 */
@Immutable
public final class NetworkException extends Exception {
    
    /**
     * Creates a new network exception.
     * 
     * @param message the message of the new exception.
     * @param throwable the cause of the new exception.
     */
    private NetworkException(@Nullable String message, @Nullable Throwable throwable) {
        super(message, throwable);
    }
    
    /**
     * Creates a new network exception with the given parameters.
     * 
     * @param message the message of the new exception.
     * @param throwable the cause of the new exception.
     * 
     * @return a new network exception with the given parameters.
     */
    @Pure
    public static @Nonnull NetworkException get(@Nullable String message, @Nullable Throwable throwable) {
        return new NetworkException(message, throwable);
    }
    
    /**
     * Creates a new network exception with the given message.
     * 
     * @param message the message of the new exception.
     * 
     * @return a new network exception with the given message.
     */
    @Pure
    public static @Nonnull NetworkException get(@Nullable String message) {
        return get(message, null);
    }
    
    /**
     * Creates a new network exception with the given cause.
     * 
     * @param throwable the cause of the new exception.
     * 
     * @return a new network exception with the given cause.
     */
    @Pure
    public static @Nonnull NetworkException get(@Nullable Throwable throwable) {
        return get(null, throwable);
    }
    
}
