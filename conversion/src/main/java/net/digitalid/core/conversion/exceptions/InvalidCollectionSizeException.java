package net.digitalid.core.conversion.exceptions;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.auxiliary.StringUtility;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception is thrown when a collection contains less elements than expected.
 */
@Immutable
public class InvalidCollectionSizeException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Expected Size -------------------------------------------------- */
    
    /**
     * Stores the expected size of the collection.
     */
    private final int expectedSize;
    
    /**
     * Returns the expected size of the collection.
     * 
     * @return the expected size of the collection.
     */
    @Pure
    public final int getExpectedSize() {
        return expectedSize;
    }
    
    /* -------------------------------------------------- Encountered Size -------------------------------------------------- */
    
    /**
     * Stores the encountered size of the collection.
     */
    private final int encounteredSize;
    
    /**
     * Returns the encountered size of the collection.
     * 
     * @return the encountered size of the collection.
     */
    @Pure
    public final int getEncounteredSize() {
        return encounteredSize;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid collection size exception with the given expected and encountered sizes.
     * 
     * @param expectedSize the expected size of the collection.
     * @param encounteredSize the encountered size of the collection.
     */
    protected InvalidCollectionSizeException(int expectedSize, int encounteredSize) {
        super("A collection should contain at least " + StringUtility.prependWithNumber(expectedSize, "element") + " but contains only " + StringUtility.prependWithNumber(encounteredSize, "element") + ".");
        
        this.expectedSize = expectedSize;
        this.encounteredSize = encounteredSize;
    }
    
    /**
     * Returns a new invalid collection size exception with the given expected and encountered sizes.
     * 
     * @param expectedSize the expected size of the collection.
     * @param encounteredSize the encountered size of the collection.
     * 
     * @return a new invalid collection size exception with the given expected and encountered sizes.
     */
    @Pure
    public static @Nonnull InvalidCollectionSizeException get(int expectedSize, int encounteredSize) {
        return new InvalidCollectionSizeException(expectedSize, encounteredSize);
    }
    
}
