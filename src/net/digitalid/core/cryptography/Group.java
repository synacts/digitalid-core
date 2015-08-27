package net.digitalid.core.cryptography;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.storable.BlockBasedSimpleNonConceptFactory;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.BytesWrapper;
import net.digitalid.core.wrappers.IntegerWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models a multiplicative group.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Group implements Storable<Group> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code modulus.group@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULUS = SemanticType.map("modulus.group@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code order.group@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ORDER = SemanticType.map("order.group@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code group@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("group@core.digitalid.net").load(TupleWrapper.TYPE, MODULUS, ORDER);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Modulus –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the modulus of this group.
     * 
     * @invariant modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is always positive.";
     */
    private final @Nonnull BigInteger modulus;
    
    /**
     * Returns the modulus of this group.
     * 
     * @return the modulus of this group.
     * 
     * @ensure modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is always positive.";
     */
    @Pure
    public @Nonnull BigInteger getModulus() {
        return modulus;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new multiplicative group with the given modulus and the given order (if not null).
     * 
     * @param modulus the modulus of the new group.
     * @param order the order of the new group or null if it is unknown.
     * 
     * @require modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is positive.";
     * @require order == null || order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(modulus) == -1 : "The order is either unknown (indicated with null) or positive and smaller than the modulus.";
     */
    Group(@Nonnull BigInteger modulus, @Nullable BigInteger order) {
        assert modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is positive.";
        assert order == null || order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(modulus) == -1 : "The order is either unknown (indicated with null) or positive and smaller than the modulus.";
        
        this.modulus = modulus;
        this.order = order;
    }
    
    
    
    /**
     * Returns whether the order of this group is known.
     * 
     * @return whether the order of this group is known.
     */
    @Pure
    public boolean hasOrder() {
        return order != null;
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
    public @Nonnull Element getElement(@Nonnull BigInteger value) {
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
    public @Nonnull Element getElement(@Nonnull Block block) throws InvalidEncodingException {
        return new Element(this, block);
    }
    
    /**
     * Returns a random element in this group.
     * 
     * @return a random element in this group.
     */
    @Pure
    public @Nonnull Element getRandomElement() {
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
    public @Nonnull Exponent getRandomExponent() {
        return getRandomExponent(modulus.bitLength() + 4);
    }
    
    /**
     * Returns a random exponent in this group of the given bit length.
     * 
     * @return a random exponent in this group of the given bit length.
     */
    @Pure
    public @Nonnull Exponent getRandomExponent(int bitLength) {
        final @Nonnull Random random = new SecureRandom();
        
        if (order == null) {
            return new Exponent(new BigInteger(bitLength, random));
        } else {
            while (true) {
                final @Nonnull BigInteger value = new BigInteger(Math.min(order.bitLength(), bitLength), random);
                if (value.compareTo(order) == -1) return new Exponent(value);
            }
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Group)) return false;
        final @Nonnull Group other = (Group) object;
        return this.modulus.equals(other.modulus);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return modulus.hashCode();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Group [Modulus: " + modulus.bitLength() + " bits, Order: " + (order == null ? "unknown" : order.bitLength() + "bits") + "]";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static class Factory extends BlockBasedSimpleNonConceptFactory<Group> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull Group group) {
            final @Nonnull FreezableArray<Block> elements =  FreezableArray.get(2);
            elements.set(0, IntegerWrapper.encodeNonNullable(MODULUS, group.modulus));
            elements.set(1, order == null ? null : new IntegerWrapper(ORDER, order).toBlock());
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull Group decodeNonNullable(@Nonnull Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
            
            this.modulus = new IntegerWrapper(tuple.getNonNullableElement(0)).getValue();
            if (modulus.compareTo(BigInteger.ZERO) != 1) throw new InvalidEncodingException("The modulus has to be positive.");
            
            this.order = tuple.isElementNull(1) ? null : new IntegerWrapper(tuple.getNonNullableElement(1)).getValue();
            if (order != null && (order.compareTo(BigInteger.ZERO) != 1 || order.compareTo(modulus) != -1)) throw new InvalidEncodingException("The order has to be either unknown (indicated with null) or positive and smaller than the modulus.");
            return new InitializationVector(BytesWrapper.decodeNonNullable(block));
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final Factory FACTORY = new Factory();
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return FACTORY;
    }
    
}
