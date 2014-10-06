package ch.virtualid.exceptions.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import javax.annotation.Nonnull;

/**
 * This class enumerates the various packet errors.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public enum PacketError implements Blockable {
    INTERNAL(0), // The error code for an internal problem.
    EXTERNAL(1), // The error code for an external problem.
    PACKET(2), // The error code for an invalid packet.
    ENCRYPTION(3), // The error code for an invalid encryption.
    SIGNATURE(4), // The error code for an invalid signature.
    COMPRESSION(5), // The error code for an invalid compression.
    REQUEST(6), // The error code for an invalid request type.
    RESPONSE(7), // The error code for an invalid response type.
    IDENTIFIER(8), // The error code for an invalid identifier as subject.
    AUTHORIZATION(9), // The error code for an insufficient authorization.
    KEYROTATION(10); // The error code for a required key rotation.
    
    
    /**
     * Stores the semantic type {@code code.error.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("code.error.packet@virtualid.ch").load(Int8Wrapper.TYPE);
    
    /**
     * Returns the packet error encoded by the given block.
     * 
     * @param block the block containing the packet error.
     * 
     * @return the packet error encoded by the given block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull PacketError get(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final byte value = new Int8Wrapper(block).getValue();
        for (final @Nonnull PacketError packetError : values()) {
            if (packetError.value == value) return packetError;
        }
        throw new InvalidEncodingException("The value '" + value + "' does not encode a packet error.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new Int8Wrapper(TYPE, getValue()).toBlock();
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
        final @Nonnull String string = name().toLowerCase();
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
}
