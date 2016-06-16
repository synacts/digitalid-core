package net.digitalid.core.conversion.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.block.Block;

/**
 * This exception is thrown when the offset within a block is invalid.
 */
@Immutable
public class InvalidBlockOffsetException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Offset -------------------------------------------------- */
    
    /**
     * Stores the offset within the block.
     */
    private final int offset;
    
    /**
     * Returns the offset within the block.
     * 
     * @return the offset within the block.
     */
    @Pure
    public final int getOffset() {
        return offset;
    }
    
    /* -------------------------------------------------- Length -------------------------------------------------- */
    
    /**
     * Stores the length within the block.
     */
    private final int length;
    
    /**
     * Returns the length within the block.
     * 
     * @return the length within the block.
     */
    @Pure
    public final int getLength() {
        return length;
    }
    
    /* -------------------------------------------------- Block -------------------------------------------------- */
    
    /**
     * Stores the block which is exceeded.
     */
    private final @Nonnull Block block;
    
    /**
     * Returns the block which is exceeded.
     * 
     * @return the block which is exceeded.
     */
    @Pure
    public final @Nonnull Block getBlock() {
        return block;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid block offset exception with the given offset, length and block.
     * 
     * @param offset the offset within the block.
     * @param length the length within the block.
     * @param block the block which is exceeded.
     */
    protected InvalidBlockOffsetException(int offset, int length, @Nonnull Block block) {
        super("The offset " + offset + " and length " + length + " exceed the block of length " + block.getLength() + ".");
        
        this.offset = offset;
        this.length = length;
        this.block = block;
    }
    
    /**
     * Returns a new invalid block offset exception with the given offset, length and block.
     * 
     * @param offset the offset within the block.
     * @param length the length within the block.
     * @param block the block which is exceeded.
     * 
     * @return a new invalid block offset exception with the given offset, length and block.
     */
    @Pure
    public static @Nonnull InvalidBlockOffsetException get(int offset, int length, @Nonnull Block block) {
        return new InvalidBlockOffsetException(offset, length, block);
    }
    
}
