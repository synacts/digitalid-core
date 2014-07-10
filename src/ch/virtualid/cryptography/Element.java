package ch.virtualid.cryptography;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import javax.annotation.Nonnull;

/**
 * An element is a number in a certain group.
 * 
 * @invariant getValue().compareTo(BigInteger.ZERO) >= 0 && getValue().compareTo(getGroup().getModulus()) == -1 : "The value is non-negative and smaller than the group modulus.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Element extends Number implements Immutable {
    
    /**
     * Stores the semantic type {@code element.group@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("element.group@virtualid.ch").load(IntegerWrapper.TYPE);
    
    
    /**
     * Stores the group of this element.
     */
    private final @Nonnull Group group;
    
    /**
     * Creates a new element in the given group with the given value.
     * 
     * @param group the group of the new element.
     * @param value the value of the new element.
     */
    Element(@Nonnull Group group, @Nonnull BigInteger value) {
        super(value.mod(group.getModulus()));
        this.group = group;
        
        assert getValue().compareTo(BigInteger.ZERO) >= 0 && getValue().compareTo(getGroup().getModulus()) == -1 : "The value is non-negative and smaller than the group modulus.";
    }
    
    /**
     * Creates a new element in the given group with the value given in the block.
     * 
     * @param group the group of the new element.
     * @param block the block encoding the value.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    Element(@Nonnull Group group, @Nonnull Block block) throws InvalidEncodingException {
        super(block);
        this.group = group;
        
        assert getValue().compareTo(BigInteger.ZERO) >= 0 && getValue().compareTo(getGroup().getModulus()) == -1 : "The value is non-negative and smaller than the group modulus.";
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Returns the group of this element.
     * 
     * @return the group of this element.
     */
    @Pure
    public @Nonnull Group getGroup() {
        return group;
    }
    
    /**
     * Returns whether this element is in the given group.
     * 
     * @param group the group of interest.
     * 
     * @return whether this element is in the given group.
     */
    @Pure
    public boolean isElement(@Nonnull Group group) {
        return getGroup().equals(group);
    }
    
    /**
     * Adds the given element to this element.
     * 
     * @param element the element to be added.
     * 
     * @return the sum of this and the given element.
     * 
     * @require getGroup().equals(element.getGroup()) : "Both elements are in the same group.";
     * 
     * @ensure return.getGroup().equals(getGroup()) : "The returned element is in the same group.";
     */
    @Pure
    public @Nonnull Element add(@Nonnull Element element) {
        assert getGroup().equals(element.getGroup()) : "Both elements are in the same group.";
        
        return new Element(getGroup(), getValue().add(element.getValue()));
    }
    
    /**
     * Subtracts the given element from this element.
     * 
     * @param element the element to be subtracted.
     * 
     * @return the difference between this and the given element.
     * 
     * @require getGroup().equals(element.getGroup()) : "Both elements are in the same group.";
     * 
     * @ensure return.getGroup().equals(getGroup()) : "The returned element is in the same group.";
     */
    @Pure
    public @Nonnull Element subtract(@Nonnull Element element) {
        assert getGroup().equals(element.getGroup()) : "Both elements are in the same group.";
        
        return new Element(getGroup(), getValue().subtract(element.getValue()));
    }
    
    /**
     * Multiplies this element with the given element.
     * 
     * @param element the element to be multiplied.
     * 
     * @return the product of this and the given element.
     * 
     * @require getGroup().equals(element.getGroup()) : "Both elements are in the same group.";
     * 
     * @ensure return.getGroup().equals(getGroup()) : "The returned element is in the same group.";
     */
    @Pure
    public @Nonnull Element multiply(@Nonnull Element element) {
        assert getGroup().equals(element.getGroup()) : "Both elements are in the same group.";
        
        return new Element(getGroup(), getValue().multiply(element.getValue()));
    }
    
    /**
     * Inverses this element.
     * 
     * @return the multiplicative inverse of this element.
     * 
     * @require isRelativelyPrime() : "The element is relatively prime to the group modulus.";
     * 
     * @ensure return.getGroup().equals(getGroup()) : "The returned element is in the same group.";
     */
    @Pure
    public @Nonnull Element inverse() {
        assert isRelativelyPrime() : "The element is relatively prime to the group modulus.";
        
        return new Element(getGroup(), getValue().modInverse(getGroup().getModulus()));
    }
    
    /**
     * Returns whether the element is relatively prime to the group modulus.
     * 
     * @return whether the element is relatively prime to the group modulus.
     */
    @Pure
    public boolean isRelativelyPrime() {
        return getValue().gcd(getGroup().getModulus()).compareTo(BigInteger.ONE) == 0;
    }
    
    /**
     * Raises this element by the given exponent.
     * 
     * @param exponent the exponent to be raised by.
     * 
     * @return this element raised by the given exponent.
     * 
     * @ensure return.getGroup().equals(getGroup()) : "The returned element is in the same group.";
     */
    @Pure
    public @Nonnull Element pow(@Nonnull Exponent exponent) {
        return pow(exponent.getValue());
    }
    
    /**
     * Raises this element by the given exponent.
     * 
     * @param exponent the exponent to be raised by.
     * 
     * @return this element raised by the given exponent.
     * 
     * @ensure return.getGroup().equals(getGroup()) : "The returned element is in the same group.";
     */
    @Pure
    public @Nonnull Element pow(@Nonnull BigInteger exponent) {
        return new Element(getGroup(), getValue().modPow(exponent, getGroup().getModulus()));
    }
    
    /**
     * Returns whether the element is equal to the neutral element.
     * 
     * @return whether the element is equal to the neutral element.
     */
    @Pure
    public boolean isOne() {
        return getValue().equals(BigInteger.ONE);
    }
    
}
