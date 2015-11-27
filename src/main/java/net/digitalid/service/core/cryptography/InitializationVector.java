package net.digitalid.service.core.cryptography;

import java.security.SecureRandom;
import javax.annotation.Nonnull;
import javax.crypto.spec.IvParameterSpec;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.BytesWrapper;
import net.digitalid.service.core.block.wrappers.EncryptionWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.InternalException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.database.declaration.ColumnDeclaration;

/**
 * The random initialization vector ensures that multiple {@link EncryptionWrapper encryptions} of the same {@link Block block} are different.
 */
@Immutable
public final class InitializationVector extends IvParameterSpec implements XDF<InitializationVector, Object>, SQL<InitializationVector, Object> {
    
    /* -------------------------------------------------- Generator -------------------------------------------------- */
    
    /**
     * Returns an array of 16 random bytes.
     * 
     * @return an array of 16 random bytes.
     * 
     * @ensure return.length == 16 : "The array contains 16 bytes.";
     */
    @Pure
    private static @Nonnull @Validated byte[] getRandomBytes() {
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new initialization vector with the given bytes.
     * 
     * @param bytes the bytes of the new initialization vector.
     * 
     * @require bytes.length == 16 : "The array contains 16 bytes.";
     */
    private InitializationVector(@Nonnull @Validated byte[] bytes) {
        super(bytes);
        
        assert bytes.length == 16 : "The array contains 16 bytes.";
    }
    
    /**
     * Returns a new initialization vector with the given bytes.
     * 
     * @param bytes the bytes of the new initialization vector.
     * 
     * @return a new initialization vector with the given bytes.
     * 
     * @require bytes.length == 16 : "The array contains 16 bytes.";
     */
    @Pure
    public static @Nonnull InitializationVector get(@Nonnull @Validated byte[] bytes) {
        return new InitializationVector(bytes);
    }
    
    /**
     * Returns a random initialization vector.
     * 
     * @return a random initialization vector.
     */
    @Pure
    public static @Nonnull InitializationVector getRandom() {
        return get(getRandomBytes());
    }
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter of this class.
     */
    private static final @Nonnull AbstractNonRequestingKeyConverter<InitializationVector, Object, byte[], Object> KEY_CONVERTER = new AbstractNonRequestingKeyConverter<InitializationVector, Object, byte[], Object>() {
        
        @Pure
        @Override
        public boolean isValid(@Nonnull byte[] bytes) {
            return bytes.length == 16;
        }
        
        @Pure
        @Override
        public @Nonnull @Validated byte[] convert(@Nonnull InitializationVector vector) {
            return vector.getIV();
        }
        
        @Pure
        @Override
        public @Nonnull InitializationVector recover(@Nonnull Object none, @Nonnull @Validated byte[] bytes) throws InvalidEncodingException, InternalException {
            return new InitializationVector(bytes);
        }
        
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code initialization.vector@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("initialization.vector@core.digitalid.net").load(BytesWrapper.XDF_TYPE);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<InitializationVector, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, BytesWrapper.getValueXDFConverter(TYPE));
    
    @Pure
    @Override
    public @Nonnull AbstractNonRequestingXDFConverter<InitializationVector, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("vector", BytesWrapper.SQL_TYPE);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<InitializationVector, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, BytesWrapper.getValueSQLConverter(DECLARATION));
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<InitializationVector, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<InitializationVector, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
