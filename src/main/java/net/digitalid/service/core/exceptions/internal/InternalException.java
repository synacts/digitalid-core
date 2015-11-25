package net.digitalid.service.core.exceptions.internal;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.logger.Log;

/**
 * An internal exception indicates a wrong use of the library.
 */
@Immutable
public class InternalException extends Exception {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new internal exception with the given message.
     * 
     * @param message a string explaining the illegal operation.
     */
    protected InternalException(@Nonnull String message) {
        super(message);
        
        Log.error("An internal exception occurred.", this);
    }
    
    /**
     * Returns a new internal exception with the given message.
     * 
     * @param message a string explaining the illegal operation.
     * 
     * @return a new internal exception with the given message.
     */
    @Pure
    public static @Nonnull InternalException get(@Nonnull String message) {
        return new InternalException(message);
    }
    
}
