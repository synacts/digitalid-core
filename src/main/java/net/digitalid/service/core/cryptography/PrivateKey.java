package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.IntegerWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.entity.annotations.Matching;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.sql.XDFConverterBasedSQLConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;

/**
 * This class stores the groups and exponents of a host's private key.
 */
@Immutable
public final class PrivateKey implements XDF<PrivateKey, Object>, SQL<PrivateKey, Object> {
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code composite.group.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType COMPOSITE_GROUP = SemanticType.map("composite.group.private.key.host@core.digitalid.net").load(GroupWithKnownOrder.TYPE);
    
    /**
     * Stores the semantic type {@code p.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType P = SemanticType.map("p.private.key.host@core.digitalid.net").load(IntegerWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code q.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType Q = SemanticType.map("q.private.key.host@core.digitalid.net").load(IntegerWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code d.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType D = SemanticType.map("d.private.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code square.group.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType SQUARE_GROUP = SemanticType.map("square.group.private.key.host@core.digitalid.net").load(GroupWithKnownOrder.TYPE);
    
    /**
     * Stores the semantic type {@code x.private.key.host@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType X = SemanticType.map("x.private.key.host@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code private.key.host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("private.key.host@core.digitalid.net").load(TupleWrapper.XDF_TYPE, COMPOSITE_GROUP, P, Q, D, SQUARE_GROUP, X);
    
    /* -------------------------------------------------- Composite Group -------------------------------------------------- */
    
    /**
     * Stores the composite group of this private key.
     */
    private final @Nonnull GroupWithKnownOrder compositeGroup;
    
    /**
     * Returns the composite group of this private key.
     * 
     * @return the composite group of this private key.
     */
    @Pure
    public @Nonnull GroupWithKnownOrder getCompositeGroup() {
        return compositeGroup;
    }
    
    /* -------------------------------------------------- Prime Factors -------------------------------------------------- */
    
    /**
     * Stores the first prime factor of the composite group's modulus.
     */
    private final @Nonnull BigInteger p;
    
    /**
     * Stores the second prime factor of the composite group's modulus.
     */
    private final @Nonnull BigInteger q;
    
    /* -------------------------------------------------- Decryption Exponent -------------------------------------------------- */
    
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
    public @Nonnull @Matching Element powD(@Nonnull @Matching Element c) {
        assert c.getGroup().equals(compositeGroup) : "The element belongs to the composite group.";
        
        return powD(c.getValue());
    }
    
    /* -------------------------------------------------- Square Group -------------------------------------------------- */
    
    /**
     * Stores the square group of this private key.
     */
    private final @Nonnull GroupWithKnownOrder squareGroup;
    
    /**
     * Returns the square group of this private key.
     * 
     * @return the square group of this private key.
     */
    @Pure
    public @Nonnull GroupWithKnownOrder getSquareGroup() {
        return squareGroup;
    }
    
    /* -------------------------------------------------- Decryption Exponent -------------------------------------------------- */
    
    /**
     * Stores the decryption exponent x of this private key.
     */
    private final @Nonnull Exponent x;
    
    /**
     * Returns the exponent x of this private key.
     * 
     * @return the exponent x of this private key.
     */
    @Pure
    public @Nonnull Exponent getX() {
        return x;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new private key with the given groups and exponents.
     * 
     * @param compositeGroup the composite group of the private key.
     * @param d the decryption exponent of the private key.
     * @param squareGroup the square group of the private key.
     * @param x the decryption exponent of the private key.
     * 
     * @require compositeGroup.getModulus().equals(p.multiply(q)) : "The modulus of the composite group is the product of p and q.";
     */
    private PrivateKey(@Nonnull GroupWithKnownOrder compositeGroup, @Nonnull BigInteger p, @Nonnull BigInteger q, @Nonnull Exponent d, @Nonnull GroupWithKnownOrder squareGroup, @Nonnull Exponent x) {
        assert compositeGroup.getModulus().equals(p.multiply(q)) : "The modulus of the composite group is the product of p and q.";
        
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
     * Creates a new private key with the given groups and exponents.
     * 
     * @param compositeGroup the composite group of the private key.
     * @param d the decryption exponent of the private key.
     * @param squareGroup the square group of the private key.
     * @param x the decryption exponent of the private key.
     * 
     * @return a new private key with the given groups and exponents.
     * 
     * @require compositeGroup.getModulus().equals(p.multiply(q)) : "The modulus of the composite group is the product of p and q.";
     */
    @Pure
    public static @Nonnull PrivateKey get(@Nonnull GroupWithKnownOrder compositeGroup, @Nonnull BigInteger p, @Nonnull BigInteger q, @Nonnull Exponent d, @Nonnull GroupWithKnownOrder squareGroup, @Nonnull Exponent x) {
        return new PrivateKey(compositeGroup, p, q, d, squareGroup, x);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractNonRequestingXDFConverter<PrivateKey, Object> {
        
        /**
         * Creates a new XDF converter.
         */
        private XDFConverter() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull PrivateKey privateKey) {
            final @Nonnull FreezableArray<Block> elements = FreezableArray.get(6);
            elements.set(0, ConvertToXDF.nonNullable(privateKey.compositeGroup, COMPOSITE_GROUP));
            elements.set(1, IntegerWrapper.encodeNonNullable(P, privateKey.p));
            elements.set(2, IntegerWrapper.encodeNonNullable(Q, privateKey.q));
            elements.set(3, ConvertToXDF.nonNullable(privateKey.d, D));
            elements.set(4, ConvertToXDF.nonNullable(privateKey.squareGroup, SQUARE_GROUP));
            elements.set(5, ConvertToXDF.nonNullable(privateKey.x, X));
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull PrivateKey decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("private.key.host@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(5);
            final @Nonnull GroupWithKnownOrder compositeGroup = GroupWithKnownOrder.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(0));
            final @Nonnull BigInteger p = IntegerWrapper.decodeNonNullable(elements.getNonNullable(1));
            final @Nonnull BigInteger q = IntegerWrapper.decodeNonNullable(elements.getNonNullable(2));
            final @Nonnull Exponent d = Exponent.get(elements.getNonNullable(3));
            final @Nonnull GroupWithKnownOrder squareGroup = GroupWithKnownOrder.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(4));
            final @Nonnull Exponent x = Exponent.get(elements.getNonNullable(5));
            
            if (!compositeGroup.getModulus().equals(p.multiply(q))) throw new InvalidEncodingException("The modulus of the composite group has to be the product of p and q.");
            
            return new PrivateKey(compositeGroup, p, q, d, squareGroup, x);
        }
        
    }
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter();
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<PrivateKey, Object> SQL_CONVERTER = XDFConverterBasedSQLConverter.get(XDF_CONVERTER);
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<PrivateKey, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<PrivateKey, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
