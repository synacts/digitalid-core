package net.digitalid.core.exceptions.packet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadonlyArray;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.StringWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This exception indicates an error in the encoding or content of a packet.
 * 
 * @see PacketError
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class PacketException extends Exception implements Blockable {
    
    /**
     * Stores the semantic type {@code message.error.packet@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MESSAGE = SemanticType.create("message.error.packet@core.digitalid.net").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code error.packet@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("error.packet@core.digitalid.net").load(TupleWrapper.TYPE, PacketError.TYPE, MESSAGE);
    
    
    /**
     * Stores the logger for packet exceptions.
     */
    private static final @Nonnull Logger LOGGER = new Logger("Packets.log");
    
    
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
        this(error, message, null);
    }
    
    /**
     * Creates a new packet exception with the given error, message and cause.
     * 
     * @param error the code indicating the kind of error.
     * @param message a string explaining the exception.
     * @param cause the cause of the packet exception.
     */
    public PacketException(@Nonnull PacketError error, @Nonnull String message, @Nullable Throwable cause) {
        this(error, message, cause, false);
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
        super("(" + error.getName() + ") " + message, cause);
        
        this.error = error;
        this.remote = remote;
        LOGGER.log(Level.WARNING, "A problem occurred", this);
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
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(2);
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
