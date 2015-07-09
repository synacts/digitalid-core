package net.digitalid.core.cryptography;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.crypto.spec.IvParameterSpec;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Database;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.SQLizable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.DataWrapper;
import net.digitalid.core.wrappers.EncryptionWrapper;

/**
 * The random initialization vector ensures that multiple {@link EncryptionWrapper encryptions} of the same {@link Block block} are different.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class InitializationVector extends IvParameterSpec implements Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code initialization.vector@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("initialization.vector@core.digitalid.net").load(DataWrapper.TYPE);
    
    
    /**
     * Returns an array of 16 random bytes.
     * 
     * @return an array of 16 random bytes.
     * 
     * @ensure return.length == 16 : "The array contains 16 bytes.";
     */
    private static byte[] getRandomBytes() {
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return  bytes;
    }
    
    /**
     * Creates a new random initialization vector.
     */
    public InitializationVector() {
        super(getRandomBytes());
    }
    
    /**
     * Creates a new initialization vector with the given bytes.
     * 
     * @param bytes the bytes of the new initialization vector.
     * 
     * @require bytes.length == 16 : "The array contains 16 bytes.";
     */
    public InitializationVector(@Nonnull byte[] bytes) {
        super(bytes);
        
        assert bytes.length == 16 : "The array contains 16 bytes.";
    }
    
    /**
     * Creates a new initialization vector from the given block.
     * 
     * @param block the block containing the initialization vector.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public InitializationVector(@Nonnull Block block) throws InvalidEncodingException {
        super(new DataWrapper(block).getData());
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        if (block.getLength() != 17) throw new InvalidEncodingException("An initialization vector has to be 16 bytes long.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new DataWrapper(TYPE, getIV()).toBlock();
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Database.getConfiguration().VECTOR();
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull InitializationVector get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return new InitializationVector(resultSet.getBytes(columnIndex));
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setBytes(parameterIndex, getIV());
    }
    
}
