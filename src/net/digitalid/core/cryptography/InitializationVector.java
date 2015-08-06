package net.digitalid.core.cryptography;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.spec.IvParameterSpec;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.SQLType;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.storable.SimpleDecodingFactory;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.BytesWrapper;
import net.digitalid.core.wrappers.EncryptionWrapper;

/**
 * The random initialization vector ensures that multiple {@link EncryptionWrapper encryptions} of the same {@link Block block} are different.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class InitializationVector extends IvParameterSpec implements Storable<InitializationVector> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code initialization.vector@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("initialization.vector@core.digitalid.net").load(BytesWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Generator –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns an array of 16 random bytes.
     * 
     * @return an array of 16 random bytes.
     * 
     * @ensure return.length == 16 : "The array contains 16 bytes.";
     */
    @Pure
    private static byte[] getRandomBytes() {
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new initialization vector with the given bytes.
     * 
     * @param bytes the bytes of the new initialization vector.
     * 
     * @require bytes.length == 16 : "The array contains 16 bytes.";
     */
    private InitializationVector(@Nonnull byte[] bytes) {
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
    public static @Nonnull InitializationVector get(@Nonnull byte[] bytes) {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static class Factory extends SimpleDecodingFactory<InitializationVector> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(TYPE, Column.get("vector", SQLType.VECTOR));
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull InitializationVector vector) {
            return BytesWrapper.encodeNonNullable(TYPE, vector.getIV());
        }
        
        @Pure
        @Override
        public @Nonnull InitializationVector decodeNonNullable(@Nonnull Block block) throws InvalidEncodingException {
            if (block.getLength() != 17) throw new InvalidEncodingException("An initialization vector has to be 16 bytes long.");
            return new InitializationVector(BytesWrapper.decodeNonNullable(block));
        }
        
        @Pure
        @Override
        protected @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull InitializationVector vector) {
            return FreezableArray.getNonNullable(Block.toString(vector.getIV()));
        }
        
        @Override
        @NonCommitting
        public void setNonNullable(@Nonnull InitializationVector vector, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setBytes(parameterIndex, vector.getIV());
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable InitializationVector getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable byte[] bytes = resultSet.getBytes(columnIndex);
            return bytes == null ? null : new InitializationVector(bytes);
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
