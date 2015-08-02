package net.digitalid.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Blockable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.IntegerWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class stores the groups and exponents of a host's private key.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class PrivateKey implements Blockable {
    
    /**
     * Stores the semantic type {@code composite.group.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType COMPOSITE_GROUP = SemanticType.map("composite.group.private.key.host@core.digitalid.net").load(Group.TYPE);
    
    /**
     * Stores the semantic type {@code p.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType P = SemanticType.map("p.private.key.host@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code q.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType Q = SemanticType.map("q.private.key.host@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code d.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType D = SemanticType.map("d.private.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code square.group.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SQUARE_GROUP = SemanticType.map("square.group.private.key.host@core.digitalid.net").load(Group.TYPE);
    
    /**
     * Stores the semantic type {@code x.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType X = SemanticType.map("x.private.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code private.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("private.key.host@core.digitalid.net").load(TupleWrapper.TYPE, COMPOSITE_GROUP, P, Q, D, SQUARE_GROUP, X);
    
    
    /**
     * Stores the composite group of this private key.
     * 
     * @invariant compositeGroup.hasOrder() : "The order of the composite group is known.";
     */
    private final @Nonnull Group compositeGroup;
    
    /**
     * Stores the first prime factor of the composite group's modulus.
     */
    private final @Nonnull BigInteger p;
    
    /**
     * Stores the second prime factor of the composite group's modulus.
     */
    private final @Nonnull BigInteger q;
    
    /**
     * Stores the decryption exponent d of this private key.
     */
    private final @Nonnull Exponent d;
    
    /**
     * Stores the decryption exponent in the subgroup of p.
     */
    private final @Nonnull BigInteger dMod_pMinus1;
    
    /**
     * Stores the decryption exponent in the subgroup of q.
     */
    private final @Nonnull BigInteger dMod_qMinus1;
    
    /**
     * Stores the identity of p's subgroup in the Chinese Remainder Theorem.
     */
    private final @Nonnull BigInteger pIdentityCRT;
    
    /**
     * Stores the identity of q's subgroup in the Chinese Remainder Theorem.
     */
    private final @Nonnull BigInteger qIdentityCRT;
    
    /**
     * Stores the square group of this private key.
     * 
     * @invariant squareGroup.hasOrder() : "The order of the square group is known.";
     */
    private final @Nonnull Group squareGroup;
    
    /**
     * Stores the decryption exponent x of this private key.
     */
    private final @Nonnull Exponent x;
    
    /**
     * Creates a new private key with the given groups and exponents.
     * 
     * @param compositeGroup the composite group of the private key.
     * @param d the decryption exponent of the private key.
     * @param squareGroup the square group of the private key.
     * @param x the decryption exponent of the private key.
     * 
     * @require compositeGroup.getModulus().equals(p.multiply(q)) : "The modulus of the composite group is the product of p and q.";
     * @require compositeGroup.hasOrder() : "The order of the composite group is known.";
     * @require squareGroup.hasOrder() : "The order of the square group is known.";
     */
    PrivateKey(@Nonnull Group compositeGroup, @Nonnull BigInteger p, @Nonnull BigInteger q, @Nonnull Exponent d, @Nonnull Group squareGroup, @Nonnull Exponent x) {
        assert compositeGroup.getModulus().equals(p.multiply(q)) : "The modulus of the composite group is the product of p and q.";
        assert compositeGroup.hasOrder() : "The order of the composite group is known.";
        assert squareGroup.hasOrder() : "The order of the square group is known.";
        
        this.compositeGroup = compositeGroup;
        this.p = p;
        this.q = q;
        this.d = d;
        this.squareGroup = squareGroup;
        this.x = x;
        
        @Nonnull BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        @Nonnull BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        
        this.dMod_pMinus1 = d.getValue().mod(pMinus1);
        this.dMod_qMinus1 = d.getValue().mod(qMinus1);
        
        this.pIdentityCRT = q.modInverse(p).multiply(q).mod(compositeGroup.getModulus());
        this.qIdentityCRT = p.modInverse(q).multiply(p).mod(compositeGroup.getModulus());
    }
    
    /**
     * Creates a new private key with the groups and exponents that are encoded in the given block.
     * 
     * @param block the block containing the private key.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public PrivateKey(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadOnlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(5);
        this.compositeGroup = new Group(elements.getNonNullable(0));
        this.p = new IntegerWrapper(elements.getNonNullable(1)).getValue();
        this.q = new IntegerWrapper(elements.getNonNullable(2)).getValue();
        this.d = new Exponent(elements.getNonNullable(3));
        this.squareGroup = new Group(elements.getNonNullable(4));
        this.x = new Exponent(elements.getNonNullable(5));
        
        if (compositeGroup.hasNoOrder()) throw new InvalidEncodingException("The order of the composite group may not be unknown.");
        if (squareGroup.hasNoOrder()) throw new InvalidEncodingException("The order of the square group may not be unknown.");
        
        if (!compositeGroup.getModulus().equals(p.multiply(q))) throw new InvalidEncodingException("The modulus of the composite group has to be the product of p and q.");
        
        @Nonnull BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        @Nonnull BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        
        this.dMod_pMinus1 = d.getValue().mod(pMinus1);
        this.dMod_qMinus1 = d.getValue().mod(qMinus1);
        
        this.pIdentityCRT = q.modInverse(p).multiply(q).mod(compositeGroup.getModulus());
        this.qIdentityCRT = p.modInverse(q).multiply(p).mod(compositeGroup.getModulus());
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(6);
        elements.set(0, compositeGroup.toBlock().setType(COMPOSITE_GROUP));
        elements.set(1, new IntegerWrapper(P, p).toBlock());
        elements.set(2, new IntegerWrapper(Q, q).toBlock());
        elements.set(3, d.toBlock().setType(D));
        elements.set(4, squareGroup.toBlock().setType(SQUARE_GROUP));
        elements.set(5, x.toBlock().setType(X));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the composite group of this private key.
     * 
     * @return the composite group of this private key.
     * 
     * @ensure return.hasOrder() : "The order of the composite group is known.";
     */
    @Pure
    public @Nonnull Group getCompositeGroup() {
        return compositeGroup;
    }
    
    /**
     * Returns the exponent d of this private key.
     * 
     * @return the exponent d of this private key.
     */
    @Pure
    public @Nonnull Exponent getD() {
        return d;
    }
    
    /**
     * Returns the integer c raised to the power of d by using the Chinese Remainder Theorem.
     * 
     * @return the integer c raised to the power of d by using the Chinese Remainder Theorem.
     */
    @Pure
    public @Nonnull Element powD(@Nonnull BigInteger c) {
        final @Nonnull BigInteger mModP = c.modPow(dMod_pMinus1, p);
        final @Nonnull BigInteger mModQ = c.modPow(dMod_qMinus1, q);
        return compositeGroup.getElement(mModP.multiply(pIdentityCRT).add(mModQ.multiply(qIdentityCRT)));
    }
    
    /**
     * Returns the element c raised to the power of d by using the Chinese Remainder Theorem.
     * 
     * @return the element c raised to the power of d by using the Chinese Remainder Theorem.
     * 
     * @require c.getGroup().equals(compositeGroup) : "The element belongs to the composite group.";
     */
    @Pure
    public @Nonnull Element powD(@Nonnull Element c) {
        assert c.getGroup().equals(compositeGroup) : "The element belongs to the composite group.";
        
        return powD(c.getValue());
    }
    
    /**
     * Returns the square group of this private key.
     * 
     * @return the square group of this private key.
     * 
     * @ensure return.hasOrder() : "The order of the square group is known.";
     */
    @Pure
    public @Nonnull Group getSquareGroup() {
        return squareGroup;
    }
    
    /**
     * Returns the exponent x of this private key.
     * 
     * @return the exponent x of this private key.
     */
    @Pure
    public @Nonnull Exponent getX() {
        return x;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof PrivateKey)) return false;
        final @Nonnull PrivateKey other = (PrivateKey) object;
        return this.compositeGroup.equals(other.compositeGroup)
                && this.p.equals(other.p)
                && this.q.equals(other.q)
                && this.d.equals(other.d)
                && this.squareGroup.equals(other.squareGroup)
                && this.x.equals(other.x);
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + compositeGroup.hashCode();
        hash = 53 * hash + p.hashCode();
        hash = 53 * hash + q.hashCode();
        hash = 53 * hash + d.hashCode();
        hash = 53 * hash + squareGroup.hashCode();
        hash = 53 * hash + x.hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Private Key [n = " + compositeGroup.getModulus() + ", p = " + p + ", q = " + q + ", d = " + d + ", z^2 = " + squareGroup.getModulus() + ", x = " + x + "]";
    }
    
}
