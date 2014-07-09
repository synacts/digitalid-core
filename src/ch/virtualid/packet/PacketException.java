package ch.virtualid.packet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception indicates a problem in the encoding or content of a packet.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class PacketException extends Exception {
    
    /**
     * Stores the error of this exception.
     */
    private final @Nonnull PacketError error;
    
    /**
     * Creates a new packet exception with the given error.
     * 
     * @param code the error code indicating the kind of problem.
     */
    public PacketException(@Nonnull PacketError error) {
        this(error, null);
    }
    
    /**
     * Creates a new packet exception with the given error and cause.
     * 
     * @param code the error indicating the kind of problem.
     * @param cause the cause that led to this packet exception.
     */
    public PacketException(@Nonnull PacketError error, @Nullable Exception cause) {
        super("PacketException (" + error + ")", cause);
        this.error = error;
    }
    
    /**
     * Returns the error of this exception.
     * 
     * @return the error of this exception.
     */
    public @Nonnull PacketError getError() {
        return error;
    }
    
}
