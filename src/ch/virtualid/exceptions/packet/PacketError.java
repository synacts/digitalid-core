package ch.virtualid.exceptions.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * This class enumerates the various packet errors.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public enum PacketError {
    INTERNAL(0), // The error code for an internal problem.
    EXTERNAL(1), // The error code for an external problem.
    PACKET(2), // The error code for an invalid packet.
    ENCRYPTION(3), // The error code for an invalid encryption.
    SIGNATURE(4), // The error code for an invalid signature.
    COMPRESSION(5), // The error code for an invalid compression.
    REQUEST(6), // The error code for an invalid request type or invalid encoding.
    RESPONSE(7), // The error code for an invalid response type or invalid encoding.
    IDENTIFIER(8), // The error code for an invalid identifier within the request.
    AUTHORIZATION(9), // The error code for an insufficient authorization.
    KEYROTATION(10); // The error code for a required key rotation.
    
    /**
     * Returns the packet error encoded by the given value or throws an {@link InvalidEncodingException}.
     * 
     * @param value the value encoding the packet error.
     * 
     * @return the packet error encoded by the given value.
     * 
     * @throws InvalidEncodingException if the given value does not encode a packet error.
     */
    @Pure
    public static @Nonnull PacketError get(byte value) throws InvalidEncodingException {
        for (final @Nonnull PacketError packetError : values()) {
            if (packetError.value == value) return packetError;
        }
        throw new InvalidEncodingException("The value '" + value + "' does not encode a packet error.");
    }
    
    
    /**
     * Stores the byte representation of the packet error.
     */
    private final byte value;
    
    /**
     * Creates a new packet error with the given value.
     * 
     * @param value the value encoding the packet error.
     */
    private PacketError(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the byte representation of this packet error.
     * 
     * @return the byte representation of this packet error.
     */
    @Pure
    public byte getValue() {
        return value;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        final @Nonnull String name = name().toLowerCase();
        @Nonnull String article = "a";
        if ("aeiou".indexOf(name.charAt(0)) != -1) article = "an";
        return article + " " + name + " error";
    }
    
}
