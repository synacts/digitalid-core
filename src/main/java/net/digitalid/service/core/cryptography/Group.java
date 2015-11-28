package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.converter.SQL;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * This class models a multiplicative group.
 * 
 * @see GroupWithKnownOrder
 * @see GroupWithUnknownOrder
 */
@Immutable
public abstract class Group<G extends Group<G>> implements XDF<G, Object>, SQL<G, Object> {
    
    /* -------------------------------------------------- Modulus -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new multiplicative group with the given modulus.
     * 
     * @param modulus the modulus of the new group.
     */
    protected Group(@Nonnull @Positive BigInteger modulus) {
        assert modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is positive.";
        
        this.modulus = modulus;
    }
    
    /* -------------------------------------------------- Element -------------------------------------------------- */
    
    /**
     * Returns a new element with the given value in this group.
     * 
     * @param value the value of the new element.
     * 
     * @return a new element with the given value in this group.
     */
    @Pure
    public final @Nonnull Element getElement(@Nonnull BigInteger value) {
        return Element.get(this, value);
    }
    
    /**
     * Returns a new element with the encoded value in this group.
     * 
     * @param block the block that encodes the value of the new element.
     * 
     * @return a new element with the encoded value in this group.
     */
    @Pure
    public final @Nonnull Element getElement(@Nonnull @BasedOn("element.group@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return Element.get(this, block);
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
            if (value.compareTo(modulus) == -1 && value.gcd(modulus).equals(BigInteger.ONE)) { break; }
        }
        
        assert value != null;
        return Element.get(this, value);
    }
    
    /* -------------------------------------------------- Exponent -------------------------------------------------- */
    
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
        return Exponent.get(new BigInteger(bitLength, random));
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof Group)) { return false; }
        @SuppressWarnings("rawtypes")
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
