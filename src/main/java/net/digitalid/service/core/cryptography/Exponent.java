package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import net.digitalid.database.core.converter.AbstractSQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.IntegerWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * An exponent is a number that raises elements of an arbitrary group.
 */
@Immutable
public final class Exponent extends Number<Exponent, Object> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new exponent with the given value.
     * 
     * @param value the value of the new exponent.
     */
    private Exponent(@Nonnull BigInteger value) {
        super(value);
    }
    
    /**
     * Creates a new exponent with the given value.
     * 
     * @param value the value of the new exponent.
     * 
     * @return a new exponent with the given value.
     */
    @Pure
    public static @Nonnull Exponent get(@Nonnull BigInteger value) {
        return new Exponent(value);
    }
    
    /**
     * Creates a new exponent from the given block.
     * 
     * @param block the block that encodes the value of the new exponent.
     * 
     * @return a new exponent from the given block.
     */
    @Pure
    public static @Nonnull Exponent get(@Nonnull @BasedOn("exponent.group@core.digitalid.net") Block block) throws InvalidEncodingException, InternalException {
        return new Exponent(IntegerWrapper.decodeNonNullable(block));
    }
    
    /* -------------------------------------------------- Operations -------------------------------------------------- */
    
    /**
     * Adds the given exponent to this exponent.
     * 
     * @param exponent the exponent to be added.
     * 
     * @return the sum of this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent add(@Nonnull Exponent exponent) {
        return new Exponent(getValue().add(exponent.getValue()));
    }
    
    /**
     * Subtracts the given exponent from this exponent.
     * 
     * @param exponent the exponent to be subtracted.
     * 
     * @return the difference between this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent subtract(@Nonnull Exponent exponent) {
        return new Exponent(getValue().subtract(exponent.getValue()));
    }
    
    /**
     * Multiplies this exponent with the given exponent.
     * 
     * @param exponent the exponent to be multiplied.
     * 
     * @return the product of this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent multiply(@Nonnull Exponent exponent) {
        return new Exponent(getValue().multiply(exponent.getValue()));
    }
    
    /**
     * Inverses this exponent in the given group.
     * 
     * @param group a group with known order.
     * 
     * @return the multiplicative inverse of this exponent.
     * 
     * @require group.getOrder().gcd(getValue()).compareTo(BigInteger.ONE) == 0 : "The exponent is relatively prime to the group order.";
     */
    @Pure
    public @Nonnull Exponent inverse(@Nonnull GroupWithKnownOrder group) {
        assert group.getOrder().gcd(getValue()).compareTo(BigInteger.ONE) == 0 : "The exponent is relatively prime to the group order.";
        
        return new Exponent(getValue().modInverse(group.getOrder()));
    }
    
    /**
     * Returns the next (or the same) relatively prime exponent.
     * 
     * @param group a group with known order.
     * 
     * @return the next (or the same) relatively prime exponent.
     */
    @Pure
    public @Nonnull Exponent getNextRelativePrime(@Nonnull GroupWithKnownOrder group) {
        @Nonnull BigInteger next = getValue();
        while (next.gcd(group.getOrder()).compareTo(BigInteger.ONE) == 1) { next = next.add(BigInteger.ONE); }
        return new Exponent(next);
    }
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter of this class.
     */
    private static final @Nonnull AbstractNonRequestingKeyConverter<Exponent, Object, BigInteger, Object> KEY_CONVERTER = new AbstractNonRequestingKeyConverter<Exponent, Object, BigInteger, Object>() {
        
        @Pure
        @Override
        public @Nonnull BigInteger convert(@Nonnull Exponent exponent) {
            return exponent.getValue();
        }
        
        @Pure
        @Override
        public @Nonnull Exponent recover(@Nonnull Object none, @Nonnull BigInteger value) throws InvalidEncodingException, InternalException {
            return new Exponent(value);
        }
        
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code exponent.group@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("exponent.group@core.digitalid.net").load(IntegerWrapper.XDF_TYPE);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<Exponent, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, IntegerWrapper.getValueXDFConverter(TYPE));
    
    @Pure
    @Override
    public @Nonnull AbstractNonRequestingXDFConverter<Exponent, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("exponent", IntegerWrapper.SQL_TYPE);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Exponent, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, IntegerWrapper.getValueSQLConverter(DECLARATION));
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<Exponent, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<Exponent, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
