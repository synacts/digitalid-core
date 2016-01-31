package net.digitalid.core.conversion.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

/**
 * This exception is thrown when the length of a block is invalid.
 */
@Immutable
public class InvalidBlockLengthException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Expected Length -------------------------------------------------- */
    
    /**
     * Stores the expected length of the block.
     */
    private final int expectedLength;
    
    /**
     * Returns the expected length of the block.
     * 
     * @return the expected length of the block.
     */
    @Pure
    public final int getExpectedLength() {
        return expectedLength;
    }
    
    /* -------------------------------------------------- Encountered Length -------------------------------------------------- */
    
    /**
     * Stores the encountered length of the block.
     */
    private final int encounteredLength;
    
    /**
     * Returns the encountered length of the block.
     * 
     * @return the encountered length of the block.
     */
    @Pure
    public final int getEncounteredLength() {
        return encounteredLength;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid block length exception with the given expected and encountered lengths.
     * 
     * @param expectedLength the expected length of the block.
     * @param encounteredLength the encountered length of the block.
     */
    protected InvalidBlockLengthException(int expectedLength, int encounteredLength) {
        super("A block of length " + expectedLength + " was expected but a block of length " + encounteredLength + " was encountered.");
        
        this.expectedLength = expectedLength;
        this.encounteredLength = encounteredLength;
    }
    
    /**
     * Returns a new invalid block length exception with the given expected and encountered lengths.
     * 
     * @param expectedLength the expected length of the block.
     * @param encounteredLength the encountered length of the block.
     * 
     * @return a new invalid block length exception with the given expected and encountered lengths.
     */
    @Pure
    public static @Nonnull InvalidBlockLengthException get(int expectedLength, int encounteredLength) {
        return new InvalidBlockLengthException(expectedLength, encounteredLength);
    }
    
}
