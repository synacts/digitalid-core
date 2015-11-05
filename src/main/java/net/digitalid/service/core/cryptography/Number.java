package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.SQL;

/**
 * A number has a value and is {@link SQL storable}.
 * 
 * @see Element
 * @see Exponent
 */
@Immutable
public abstract class Number<E extends Number<E>> implements XDF<E, Object>, SQL<E, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the value of this number.
     */
    private final @Nonnull BigInteger value;
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new number with the given value.
     * 
     * @param value the value of the new number.
     */
    protected Number(@Nonnull BigInteger value) {
        this.value = value;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Number)) return false;
        @SuppressWarnings("rawtypes")
        final @Nonnull Number<?> other = (Number) object;
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
