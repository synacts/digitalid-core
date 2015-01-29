package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A number has a value and is {@link Blockable blockable}.
 * 
 * @see Element
 * @see Exponent
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
abstract class Number implements Immutable, Blockable {
    
    /**
     * Stores the value of this number.
     */
    private final @Nonnull BigInteger value;
    
    /**
     * Creates a new number with the given value.
     * 
     * @param value the value of the new number.
     */
    Number(@Nonnull BigInteger value) {
        this.value = value;
    }
    
    /**
     * Creates a new number from the given block.
     * 
     * @param block the block that encodes the value of the new number.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    Number(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        this.value = new IntegerWrapper(block).getValue();
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return new IntegerWrapper(getType(), value).toBlock();
    }
    
    
    /**
     * Returns the value of this number.
     * 
     * @return the value of this number.
     */
    @Pure
    public final @Nonnull BigInteger getValue() {
        return value;
    }
    
    /**
     * Returns the bit length of this number.
     * 
     * @return the bit length of this number.
     */
    @Pure
    public final int getBitLength() {
        return value.bitLength();
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Number)) return false;
        final @Nonnull Number other = (Number) object;
        return value.equals(other.value);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return value.hashCode();
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return value.toString() + " [" + value.bitLength() + " bits]";
    }
    
}
