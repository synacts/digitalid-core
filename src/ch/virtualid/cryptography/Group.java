package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.TupleWrapper;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a multiplicative group.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Group implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code modulus.group@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULUS = SemanticType.create("modulus.group@virtualid.ch").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code order.group@virtualid.ch}.
     */
    private static final @Nonnull SemanticType ORDER = SemanticType.create("order.group@virtualid.ch").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code group@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("group@virtualid.ch").load(TupleWrapper.TYPE, MODULUS, ORDER);
    
    
    /**
     * Stores the modulus of this group.
     * 
     * @invariant modulus.compareTo(BigInteger.ZERO) == 1 : "The modulus is always positive.";
     */
    private final @Nonnull BigInteger modulus;
    
    /**
     * Stores the order of this group or null if it is unknown.
     * 
     * @invariant order == null || order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(modulus) == -1 : "The order is either unknown (indicated with null) or positive and smaller than the modulus.";
     */
    private final @Nullable BigInteger order;
    
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
     * Creates a new multiplicative group with the modulus and the order encoded in the given block.
     * 
     * @param block the block containing the group.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    Group(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        
        this.modulus = new IntegerWrapper(tuple.getElementNotNull(0)).getValue();
        if (modulus.compareTo(BigInteger.ZERO) != 1) throw new InvalidEncodingException("The modulus has to be positive.");
        
        this.order = tuple.isElementNull(1) ? null : new IntegerWrapper(tuple.getElementNotNull(1)).getValue();
        if (order != null && (order.compareTo(BigInteger.ZERO) != 1 || order.compareTo(modulus) != -1)) throw new InvalidEncodingException("The order has to be either unknown (indicated with null) or positive and smaller than the modulus.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, new IntegerWrapper(MODULUS, modulus).toBlock());
        elements.set(1, order == null ? null : new IntegerWrapper(ORDER, order).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
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
    
    /**
     * Returns whether the order of this group is known.
     * 
     * @return whether the order of this group is known.
     */
    @Pure
    public boolean hasOrder() {
        return order != null;
    }
    
    /**
     * Returns whether the order of this group is unknown.
     * 
     * @return whether the order of this group is unknown.
     */
    @Pure
    public boolean hasNoOrder() {
        return order == null;
    }
    
    /**
     * Returns the order of this group.
     * 
     * @return the order of this group.
     * 
     * @require hasOrder() : "The order of this group is known.";
     * 
     * @ensure order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(modulus) == -1 : "The order is positive and smaller than the modulus.";
     */
    @Pure
    public @Nonnull BigInteger getOrder() {
        assert order != null : "The order of this group is known.";
        
        return order;
    }
    
    /**
     * Returns a new group with the same modulus but without the order.
     * 
     * @return a new group with the same modulus but without the order.
     * 
     * @require hasOrder() : "The order of this group is known.";
     * 
     * @ensure return.hasNoOrder() : "The order of the returned group is unknown.";
     */
    @Pure
    public @Nonnull Group dropOrder() {
        assert hasOrder() : "The order of this group is known.";
        
        return new Group(modulus, null);
    }
    
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
    
}
