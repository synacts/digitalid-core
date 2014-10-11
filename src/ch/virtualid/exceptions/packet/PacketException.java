package ch.virtualid.exceptions.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import static ch.virtualid.io.Level.WARNING;
import ch.virtualid.io.Logger;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This exception indicates an error in the encoding or content of a packet.
 * 
 * @see PacketError
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PacketException extends Exception implements Blockable {
    
    /**
     * Stores the semantic type {@code message.error.packet@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MESSAGE = SemanticType.create("message.error.packet@virtualid.ch").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code error.packet@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("error.packet@virtualid.ch").load(TupleWrapper.TYPE, PacketError.TYPE, MESSAGE);
    
    
    /**
     * Stores the logger for packet exceptions.
     */
    private static final @Nonnull Logger logger = new Logger("Packets.log");
    
    
    /**
     * Stores the error of this exception.
     */
    private final @Nonnull PacketError error;
    
    /**
     * Stores whether this exception was thrown remotely.
     */
    private final boolean remote;
    
    /**
     * Creates a new packet exception with the given error and message.
     * 
     * @param error the code indicating the kind of error.
     * @param message a string explaining the exception.
     */
    public PacketException(@Nonnull PacketError error, @Nonnull String message) {
        this(error, message, null, false);
    }
    
    /**
     * Creates a new packet exception with the given error, message and cause.
     * 
     * @param error the code indicating the kind of error.
     * @param message a string explaining the exception.
     * @param cause the cause of the packet exception.
     * @param remote whether it was thrown remotely.
     */
    public PacketException(@Nonnull PacketError error, @Nonnull String message, @Nullable Throwable cause, boolean remote) {
        super("(" + error + ") " + message, cause);
        
        this.error = error;
        this.remote = remote;
        logger.log(WARNING, this);
    }
    
    /**
     * Creates a new packet exception from the given block.
     * 
     * @param block the block containing the packet error.
     * 
     * @return the packet exception created from the given block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static PacketException create(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        final @Nonnull PacketError error = PacketError.get(elements.getNotNull(0));
        final @Nonnull String message = new StringWrapper(elements.getNotNull(1)).getString();
        return new PacketException(error, "A host responded with a packet error. [" + message + "]", null, true);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, error.toBlock());
        elements.set(1, new StringWrapper(MESSAGE, toString()).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
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
    
    /**
     * Returns whether this exception was thrown remotely.
     * 
     * @return whether this exception was thrown remotely.
     */
    @Pure
    public boolean isRemote() {
        return remote;
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Logger.getMessage(this);
    }
    
}
