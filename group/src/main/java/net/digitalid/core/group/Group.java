package net.digitalid.core.group;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;

import net.digitalid.core.annotations.group.GroupInterface;

import net.digitalid.utility.math.ElementSubclass;
import net.digitalid.utility.math.ExponentSubclass;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models a multiplicative group.
 * 
 * @see GroupWithKnownOrder
 * @see GroupWithUnknownOrder
 */
@Immutable
public abstract class Group implements GroupInterface {
    
    /* -------------------------------------------------- Element -------------------------------------------------- */
    
    /**
     * Returns a new element with the given value in this group.
     */
    @Pure
    public @Nonnull Element getElement(@Nonnull BigInteger value) {
        return new ElementSubclass(this, value);
    }
    
    /**
     * Returns a random element in this group.
     */
    @Pure
    public @Nonnull Element getRandomElement() {
        final @Nonnull Random random = new SecureRandom();
        @Nullable BigInteger value = null;
        
        while (true) {
            value = new BigInteger(getModulus().bitLength(), random);
            if (value.compareTo(getModulus()) == -1 && value.gcd(getModulus()).equals(BigInteger.ONE)) { break; }
        }
        
        assert value != null;
        return new ElementSubclass(this, value);
    }
    
    /* -------------------------------------------------- Exponent -------------------------------------------------- */
    
    /**
     * Returns a random exponent in this group of the given bit length.
     */
    @Pure
    public final @Nonnull Exponent getRandomExponent(@NonNegative int bitLength) {
        return new ExponentSubclass(new BigInteger(bitLength, new SecureRandom()));
    }
    
    /**
     * Returns a random exponent in this group.
     */
    @Pure
    public @Nonnull Exponent getRandomExponent() {
        return getRandomExponent(getModulus().bitLength() + 4);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    // The following methods are implemented here instead of having them generated in order to ignore the order.
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof GroupInterface)) { return false; }
        final @Nonnull GroupInterface that = (GroupInterface) object;
        return this.getModulus().equals(that.getModulus());
    }
    
    @Pure
    @Override
    public int hashCode() {
        return getModulus().hashCode();
    }
    
}
