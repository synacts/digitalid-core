package ch.virtualid.exceptions.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import javax.annotation.Nonnull;

/**
 * This exception indicates an error in the encoding or content of a packet.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PacketException extends Exception implements Blockable {
    
    /**
     * Stores the semantic type {@code error.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("error.packet@virtualid.ch").load(Int8Wrapper.TYPE);
    
    
    /**
     * Stores the error of this exception.
     */
    private final @Nonnull PacketError error;
    
    /**
     * Creates a new packet exception with the given error.
     * 
     * @param error the code indicating the kind of error.
     */
    public PacketException(@Nonnull PacketError error) {
        this(error, null);
    }
    
    /**
     * Creates a new packet exception with the given error and cause.
     * 
     * @param error the code indicating the kind of error.
     * @param cause the cause of this packet exception.
     */
    public PacketException(@Nonnull PacketError error, @Nonnull Throwable cause) {
        super("PacketException (" + error + ")", cause);
        
        this.error = error;
    }
    
    /**
     * Creates a new packet exception from the given block.
     * 
     * @param block the block containing the packet error.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public PacketException(@Nonnull Block block) throws InvalidEncodingException {
        this(PacketError.get(new Int8Wrapper(block).getValue()));
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new Int8Wrapper(TYPE, error.getValue()).toBlock();
    }
    
    
    /**
     * Returns the error of this exception.
     * 
     * @return the error of this exception.
     */
    @Pure
    public @Nonnull PacketError getError() {
        return error;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return error.toString();
    }
    
}
