package ch.virtualid.exceptions.external;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Packet;
import ch.virtualid.packet.Replay;
import javax.annotation.Nonnull;

/**
 * This exception is thrown when a replay of a packet is detected.
 * 
 * @see Replay
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ReplayDetectedException extends ExternalException implements Immutable {
    
    /**
     * Stores the packet that has been replayed.
     */
    private final @Nonnull Packet packet;
    
    /**
     * Creates a new replay detected exception with the given packet.
     * 
     * @param packet the packet that has been replayed.
     */
    public ReplayDetectedException(@Nonnull Packet packet) {
        super("The replay of a packet has been detected.");
        
        this.packet = packet;
    }
    
    /**
     * Returns the packet that has been replayed.
     * 
     * @return the packet that has been replayed.
     */
    @Pure
    public @Nonnull Packet getPacket() {
        return packet;
    }
    
}
