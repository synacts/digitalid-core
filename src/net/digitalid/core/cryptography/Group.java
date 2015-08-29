package net.digitalid.core.cryptography;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.BasedOn;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Positive;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.wrappers.Block;

/**
 * This class models a multiplicative group.
 * 
 * @see GroupWithKnownOrder
 * @see GroupWithUnknownOrder
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Group<G extends Group<G>> implements Storable<G> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Modulus –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the modulus of this group.
     */
    private final @Nonnull @Positive BigInteger modulus;
    
    /**
     * Returns the modulus of this group.
     * 
     * @return the modulus of this group.
     */
    @Pure
    public final @Nonnull @Positive BigInteger getModulus() {
        return modulus;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new multiplicative group with the given modulus.
     * 
     * @param modulus the modulus of the new group.
     */
    protected Group(@Nonnull @Positive BigInteger modulus) {
        assert modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is positive.";
        
        this.modulus = modulus;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Element –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a new element with the given value in this group.
     * 
     * @param value the value of the new element.
     * 
     * @return a new element with the given value in this group.
     */
    @Pure
    public final @Nonnull Element getElement(@Nonnull BigInteger value) {
        return new Element(this, value);
    }
    
    /**
     * Returns a new element with the encoded value in this group.
     * 
     * @param block the block that encodes the value of the new element.
     * 
     * @return a new element with the encoded value in this group.
     * 
     * @require block.getType().isBasedOn(Element.TYPE) : "The block is based on the element type.";
     */
    @Pure
    public final @Nonnull Element getElement(@Nonnull @BasedOn("element.group@core.digitalid.net") Block block) throws InvalidEncodingException {
        return new Element(this, block);
    }
    
    /**
     * Returns a random element in this group.
     * 
     * @return a random element in this group.
     */
    @Pure
    public final @Nonnull Element getRandomElement() {
        final @Nonnull Random random = new SecureRandom();
        @Nullable BigInteger value = null;
        
        while (true) {
            value = new BigInteger(modulus.bitLength(), random);
            if (value.compareTo(modulus) == -1 && value.gcd(modulus).equals(BigInteger.ONE)) break;
        }
        
        return new Element(this, value);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Exponent –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a random exponent in this group.
     * 
     * @return a random exponent in this group.
     */
    @Pure
    public final @Nonnull Exponent getRandomExponent() {
        return getRandomExponent(modulus.bitLength() + 4);
    }
    
    /**
     * Returns a random exponent in this group of the given bit length.
     * 
     * @return a random exponent in this group of the given bit length.
     */
    @Pure
    public final @Nonnull Exponent getRandomExponent(int bitLength) {
        final @Nonnull Random random = new SecureRandom();
        return new Exponent(new BigInteger(bitLength, random));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Group)) return false;
        final @Nonnull Group<?> other = (Group) object;
        return this.modulus.equals(other.modulus);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return modulus.hashCode();
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return getClass().getSimpleName() + " [Modulus: " + modulus + "]";
    }
    
}
