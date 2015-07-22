package net.digitalid.core.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Captured;
import net.digitalid.core.annotations.Exposed;
import net.digitalid.core.annotations.ExposedRecipient;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonExposedRecipient;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.ValidIndex;
import net.digitalid.core.cryptography.InitializationVector;
import net.digitalid.core.cryptography.SymmetricKey;
import net.digitalid.core.database.Database;
import net.digitalid.core.database.SQLizable;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.storable.Storable;

/**
 * A block is a sequence of bytes that is encoded according to some syntactic type.
 * In order to prevent unnecessary copying, this sequence is given by a byte array,
 * where an offset and a length is used to reference just a part of the array.
 * If a block is annotated as {@link Exposed exposed}, it {@link #isEncoding() is encoding}.
 * <p>
 * <em>Important:</em> Only share {@link #isEncoded() encoded} blocks between threads!
 * 
 * @invariant bytes != null || wrapper != null : "Either the byte array or the wrapper of this block is given.";
 * @invariant offset >= 0 : "The offset of this block in the byte array is not negative.";
 * @invariant bytes == null || length > 0 : "If this block is allocated, its length is positive.";
 * @invariant bytes == null || offset + length <= bytes.length : "If this block is allocated, it may not exceed the byte array.";
 * @invariant !isEncoded() || isAllocated() : "If the block is encoded, it is also allocated.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class Block implements SQLizable, Cloneable {
    
    // TODO: Javadoc and nullable version
    @Pure
    public static @Nonnull <V extends Storable<V>> Block fromNonNullable(@Nonnull V storable) {
        return storable.getFactory().encodeNonNullable(storable);
    }
    
    /**
     * Returns the blockable instance as a block or null if the instance is null.
     * 
     * @param blockable the instance to be returned as a block.
     * 
     * @return the blockable instance as a block or null if the instance is null.
     */
    @Pure
    public static @Nullable Block toBlock(@Nullable Blockable blockable) {
        return blockable == null ? null : blockable.toBlock();
    }
    
    /**
     * Returns the blockable instance as a block of the given type or null if the instance is null.
     * 
     * @param type the type to set for the returned block.
     * @param blockable the instance to be returned as a block.
     * 
     * @return the blockable instance as a block of the given type or null if the instance is null.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require blockable == null || type.isBasedOn(blockable.getType()) : "If the blockable instance is not null, the type is based on its type.";
     */
    @Pure
    public static @Nullable Block toBlock(@Nonnull SemanticType type, @Nullable Blockable blockable) {
        return blockable == null ? null : blockable.toBlock().setType(type);
    }
    
    
    /**
     * Asserts that the class invariant still holds.
     */
    @Pure
    private boolean invariant() {
        assert bytes != null || wrapper != null : "Either the byte array or the wrapper of this block is given.";
        assert offset >= 0 : "The offset of this block in the byte array is not negative.";
        assert bytes == null || length > 0 : "If this block is allocated, its length is positive.";
        assert bytes == null || offset + length <= bytes.length : "If this block is allocated, it may not exceed the byte array.";
        assert !isEncoded() || isAllocated() : "If the block is encoded, it is also allocated.";
        return true;
    }
    
    /**
     * Stores the semantic type of this block.
     * 
     * @invariant type.isLoaded() : "The type declaration is loaded.";
     * @invariant new.isBasedOn(old) : "The type can only be downcast.";
     */
    private @Nonnull SemanticType type;
    
    /**
     * Stores the byte array of this block.
     * Can be null in case of lazy encoding.
     */
    private @Nullable byte[] bytes = null;
    
    /**
     * Stores the offset of this block in the byte array.
     */
    private int offset = 0;
    
    /**
     * Stores the (determined and cached) length of this block.
     * A negative value indicates that the length of this block has not yet been determined.
     */
    private int length = -1;
    
    /**
     * Stores the wrapper of this block for lazy encoding or null otherwise.
     */
    private final @Nullable BlockWrapper wrapper;
    
    /**
     * Stores whether this block is already encoded and can thus no longer be written to.
     */
    private boolean encoded = false;
    
    /**
     * Allocates a new block of the given type with the given byte array.
     * 
     * @param type the semantic type of this block.
     * @param bytes the byte array of this block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require bytes.length > 0 : "The byte array is not empty.";
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    public Block(@Nonnull SemanticType type, @Captured @Nonnull byte[] bytes) {
        assert type.isLoaded() : "The type declaration is loaded.";
        assert bytes.length > 0 : "The byte array is not empty.";
        
        this.type = type;
        this.bytes = bytes;
        this.offset = 0;
        this.length = bytes.length;
        this.wrapper = null;
        this.encoded = true;
        
        assert invariant();
    }
    
    /**
     * Allocates a new block of the given type based on the given block.
     * This constructor allows syntactic types to recast a block for their internal decoding.
     * 
     * @param type the semantic type of this block.
     * @param block the block containing the byte array.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require !block.isEncoding() : "The given block is not in the process of being encoded.";
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    public Block(@Nonnull SemanticType type, @Nonnull Block block) {
        this(type, block.ensureEncoded(), 0, block.getLength());
    }
    
    /**
     * Allocates a new block with the indicated section in the byte array.
     * 
     * @param type the semantic type of this block.
     * @param block the block containing the byte array.
     * @param offset the offset of this block in the given block.
     * @param length the length of this block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require block.isEncoded() : "The given block is encoded.";
     * @require offset >= 0 : "The offset is not negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= block.getLength() : "The section fits into the given block.";
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    public Block(@Nonnull SemanticType type, @Nonnull Block block, int offset, int length) {
        assert type.isLoaded() : "The type declaration is loaded.";
        assert block.isEncoded() : "The given block is encoded.";
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= block.getLength() : "The section fits into the given block.";
        
        this.type = type;
        this.bytes = block.bytes;
        this.offset = block.offset + offset;
        this.length = length;
        this.wrapper = null;
        this.encoded = true;
        
        assert invariant();
    }
    
    /**
     * Allocates a new block of the given type with the given wrapper.
     * This constructor is used for the lazy encoding of blocks.
     * 
     * @param type the semantic type of this block.
     * @param wrapper the wrapper of this block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * 
     * @ensure !isAllocated() : "This block is not yet allocated.";
     */
    Block(@Nonnull SemanticType type, @Nonnull BlockWrapper wrapper) {
        assert type.isLoaded() : "The type declaration is loaded.";
        
        this.type = type;
        this.wrapper = wrapper;
        
        assert invariant();
    }
    
    
    /**
     * Returns the semantic type of this block.
     * <p>
     * <em>Important:</em> Do not rely on the identity of the type, but rather use
     * {@link SemanticType#isBasedOn(net.digitalid.core.identity.SemanticType)} for comparisons.
     * 
     * @return the semantic type of this block.
     * 
     * @ensure type.isLoaded() : "The type declaration is loaded.";
     * @ensure new.isBasedOn(old) : "The type can only be downcast.";
     */
    @Pure
    public @Nonnull SemanticType getType() {
        return type;
    }
    
    /**
     * Sets the semantic type of this block.
     * 
     * @param type the type to set for this block.
     * 
     * @return this block.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     * @require type.isBasedOn(getType()) : "The type can only be downcast.";
     */
    public @Nonnull Block setType(@Nonnull SemanticType type) {
        assert type.isLoaded() : "The type declaration is loaded.";
        assert type.isBasedOn(getType()) : "The type can only be downcast.";
        
        this.type = type;
        return this;
    }
    
    /**
     * Checks that the type of this block is based on the given type.
     * 
     * @param type the type to set for this block.
     * 
     * @return this block.
     * 
     * @throws InvalidEncodingException if this is not the case.
     * 
     * @require type.isLoaded() : "The type declaration is loaded.";
     */
    public @Nonnull Block checkType(@Nonnull SemanticType type) throws InvalidEncodingException {
        assert type.isLoaded() : "The type declaration is loaded.";
        
        if (!this.type.isBasedOn(type)) throw new InvalidEncodingException("The type of this block (" + this.type.getAddress() + ") is not based on the given type (" + type.getAddress() + ").");
        return this;
    }
    
    
    /**
     * Returns the byte at the given index of this block.
     * 
     * @param index the index of the byte to be returned.
     * 
     * @return the byte at the given index of this block.
     * 
     * @require !isEncoding() : "This method is not called during encoding.";
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    @NonExposedRecipient
    public @Nonnull byte getByte(@ValidIndex int index) {
        assert !isEncoding() : "This method is not called during encoding.";
        assert index >= 0 && index < getLength() : "The index is valid.";
        
        ensureEncoded();
        return bytes[offset + index];
    }
    
    /**
     * Returns the bytes of this block.
     * Use {@link #getInputStream()} if possible to avoid unnecessary copying.
     * 
     * @return the bytes of this block.
     * 
     * @require isNotEncoding() : "This method is not called during encoding.";
     * 
     * @ensure isEncoded() : "The block is encoded.";
     * @ensure result.length == getLength() : "The result has the whole length.";
     */
    @NonExposedRecipient
    public @Capturable @Nonnull byte[] getBytes() {
        return getBytes(0);
    }
    
    /**
     * Returns the bytes from the given offset to the end of this block.
     * Use {@link #getInputStream()} if possible to avoid unnecessary copying.
     * 
     * @param offset the offset of the bytes to be returned.
     * 
     * @return the bytes from the given offset to the end of this block.
     * 
     * @require isNotEncoding() : "This method is not called during encoding.";
     * @require offset >= 0 && offset < getLength() : "The offset is valid.";
     * 
     * @ensure isEncoded() : "The block is encoded.";
     * @ensure result.length == getLength() - offset : "The result has the right length.";
     */
    @NonExposedRecipient
    public @Capturable @Nonnull byte[] getBytes(int offset) {
        return getBytes(offset, getLength() - offset);
    }
    
    /**
     * Returns the given length of bytes from the given offset of this block.
     * Use {@link #getInputStream()} if possible to avoid unnecessary copying.
     * 
     * @param offset the offset of the bytes to be returned.
     * @param length the length of the bytes to be returned.
     * 
     * @return the given length of bytes from the given offset of this block.
     * 
     * @require !isEncoding() : "This method is not called during encoding.";
     * @require offset >= 0 : "The offset is non-negative.";
     * @require length >= 0 : "The length is non-negative.";
     * @require offset + length <= getLength() : "The offset and length are within this block.";
     * 
     * @ensure isEncoded() : "The block is encoded.";
     * @ensure result.length == length : "The result has the given length.";
     */
    @NonExposedRecipient
    public @Capturable @Nonnull byte[] getBytes(int offset, int length) {
        assert !isEncoding() : "This method is not called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length >= 0 : "The length is non-negative.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        ensureEncoded();
        final @Nonnull byte[] result = new byte[length];
        System.arraycopy(bytes, this.offset + offset, result, 0, length);
        return result;
    }
    
    
    /**
     * Sets the byte at the given index of this block to the given value.
     * 
     * @param index the index of the byte to be set.
     * @param value the new value of the byte at the given index.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     */
    @ExposedRecipient
    public void setByte(@ValidIndex int index, byte value) {
        assert isEncoding() : "This method may only be called during encoding.";
        assert index >= 0 && index < getLength() : "The index is valid.";
        
        bytes[offset + index] = value;
    }
    
    /**
     * Sets the bytes at the given offset of this block to the given values.
     * 
     * @param offset the offset of the bytes to be set.
     * @param values the new values of the bytes at the given offset.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     * @require offset >= 0 : "The offset is not negative.";
     * @require offset + values.length <= getLength() : "The given values may not exceed this block.";
     */
    @ExposedRecipient
    public void setBytes(int offset, @Nonnull byte[] values) {
        setBytes(offset, values, 0, values.length);
    }
    
    /**
     * Sets the bytes at the given index of this block to the indicated section of the given byte array.
     * 
     * @param index the index of the bytes to be set.
     * @param values the byte array containing the indicated section.
     * @param offset the offset of the indicated section in the byte array.
     * @param length the length of the indicated section in the byte array.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     * @require index >= 0 : "The index is not negative.";
     * @require index + length <= getLength() : "The given values may not exceed this block.";
     * @require offset >= 0 : "The offset is not negative.";
     * @require length >= 0 : "The length is non-negative.";
     * @require offset + length <= values.length : "The indicated section may not exceed the given byte array.";
     */
    @ExposedRecipient
    public void setBytes(int index, @Nonnull byte[] values, int offset, int length) {
        assert isEncoding() : "This method may only be called during encoding.";
        assert index >= 0 : "The index is not negative.";
        assert index + length <= getLength() : "The given values may not exceed this block.";
        assert offset >= 0 : "The offset is not negative.";
        assert length >= 0 : "The length is non-negative.";
        assert offset + length <= values.length : "The indicated section may not exceed the given byte array.";
        
        System.arraycopy(values, offset, bytes, this.offset + index, length);
    }
    
    
    /**
     * Returns the length of this block.
     * 
     * @return the length of this block.
     * 
     * @ensure return > 0 : "The returned length is positive (i.e. the block is not empty).";
     */
    @Pure
    public int getLength() {
        if (length < 0) {
            assert wrapper != null : "The length may only be negative in case of lazy encoding.";
            length = wrapper.determineLength();
            
            assert length > 0 : "The length is positive (i.e. the block is not empty).";
            assert invariant();
        }
        return length;
    }
    
    /**
     * Returns whether this block is allocated.
     * 
     * @return whether this block is allocated.
     */
    @Pure
    public boolean isAllocated() {
        return bytes != null;
    }
    
    /**
     * Returns whether this block is encoded.
     * 
     * @return whether this block is encoded.
     * 
     * @ensure !return || isAllocated() : "If this block is encoded, it is also allocated.";
     */
    @Pure
    public boolean isEncoded() {
        return encoded;
    }
    
    /**
     * Returns whether this block is in the process of being encoded.
     * <p>
     * <em>Important:</em> If a parameter or local variable is not annotated as {@link Exposed exposed}, it is assumed that such a block is <em>not</em> in the process of being encoded.
     * 
     * @return whether this block is in the process of being encoded.
     * 
     * @ensure return == isAllocated() && !isEncoded() : "If this block is in the process of being encoded, it is already allocated but not yet encoded.";
     */
    @Pure
    public boolean isEncoding() {
        return isAllocated() && !isEncoded();
    }
    
    /**
     * Ensures that this block is encoded.
     * 
     * @return this block.
     * 
     * @require !isEncoding() : "This method is not called during encoding.";
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    @Pure
    @NonExposedRecipient
    public @Nonnull Block ensureEncoded() {
        assert !isEncoding() : "This method is not called during encoding.";
        
        if (!isEncoded()) encode();
        return this;
    }
    
    /**
     * Encodes the byte array of this block.
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    @NonExposedRecipient
    private void encode() {
        assert !isAllocated() : "This block may not yet be allocated.";
        
        bytes = new byte[getLength()];
        assert wrapper != null : "The byte array may only be null in case of lazy encoding.";
        wrapper.encode(this);
        encoded = true;
        
        assert invariant();
    }
    
    
    /**
     * Returns the SHA-256 hash of this block.
     * 
     * @return the SHA-256 hash of this block.
     * 
     * @ensure isEncoded() : "This block is encoded.";
     * @ensure return.signum() >= 0 : "The result is positive.";
     * @ensure return.bitLength() <= Parameters.HASH : "The length of the result is at most Parameters.HASH.";
     */
    @Pure
    @NonExposedRecipient
    public @Nonnull BigInteger getHash() {
        assert !isEncoding() : "This method is not called during encoding.";
        
        ensureEncoded();
        try {
            final @Nonnull MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(bytes, offset, length);
            return new BigInteger(1, instance.digest());
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw new ShouldNeverHappenError("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
    }
    
    
    /**
     * Writes this block into the given block (and encodes this block in place if necessary).
     * This method allows syntactic types to recast a block for their internal encoding.
     * 
     * @param block the block to writeTo this block to.
     * 
     * @require block.isEncoding() : "The given block is in the process of being encoded.";
     * @require getLength() == block.getLength() : "This block has to have the same length as the given block.";
     * @require isNotEncoding() : "This block is not in the process of being encoded.";
     */
    @NonExposedRecipient
    public void writeTo(@Exposed @Nonnull Block block) {
        writeTo(block, 0, block.getLength());
    }
    
    /**
     * Writes this block into the designated section of the given block (and encodes this block in place if necessary).
     * 
     * @param block the block containing the designated section.
     * @param offset the offset of the section in the given block.
     * @param length the length of the section in the given block.
     * 
     * @require block.isEncoding() : "The given block is in the process of being encoded.";
     * @require offset >= 0 : "The offset is not negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
     * @require getLength() == length : "This block has to have the same length as the designated section.";
     */
    @NonExposedRecipient
    public void writeTo(@Exposed @Nonnull Block block, int offset, int length) {
        assert block.isEncoding() : "The given block is in the process of being encoded.";
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
        assert getLength() == length : "This block has to have the same length as the designated section.";
        assert !isEncoding() : "This block is not in the process of being encoded.";
        
        if (isEncoded()) {
            System.arraycopy(this.bytes, this.offset, block.bytes, block.offset + offset, length);
        } else {
            this.bytes = block.bytes;
            this.offset = block.offset + offset;
            this.length = length;
            assert wrapper != null : "The byte array may only be null in case of lazy encoding.";
            wrapper.encode(this);
            this.encoded = true;
        }
        
        assert invariant();
    }
    
    
    /**
     * Returns this block as a blockable object.
     * 
     * @return this block as a blockable object.
     */
    @Pure
    public @Nonnull Blockable toBlockable() {
        return new BlockableObject(this);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Block)) return false;
        final @Nonnull Block other = (Block) object;
        
        if (!getType().equals(other.getType())) return false;
        
        this.ensureEncoded();
        other.ensureEncoded();
        
        if (this.length != other.length) return false;
        
        for (int i = 0; i < length; i++) {
            if (this.bytes[this.offset + i] != other.bytes[other.offset + i]) return false;
        }
        
        return true;
    }
    
    @Pure
    @Override
    public int hashCode() {
        ensureEncoded();
        int result = 1;
        for (int i = 0; i < length; i++) {
            result = 31 * result + bytes[offset + i];
        }
        return result;
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Database.getConfiguration().BLOB();
    
    /**
     * Returns the block at the given index of the given result set.
     * 
     * @param type the semantic type of the block to be returned.
     * @param resultSet the result set whose block is to be returned.
     * @param columnIndex the index of the block to be returned.
     * 
     * @return the block at the given index of the given result set.
     */
    @NonCommitting
    public static @Nullable Block get(@Nonnull SemanticType type, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull byte[] bytes = resultSet.getBytes(columnIndex);
        if (resultSet.wasNull()) return null;
        else return new Block(type, bytes);
    }
    
    /**
     * Returns the block at the given index of the given result set.
     * Please note that the column of the block may not contain null.
     * 
     * @param type the semantic type of the block to be returned.
     * @param resultSet the result set whose block is to be returned.
     * @param columnIndex the index of the block to be returned.
     * 
     * @return the block at the given index of the given result set.
     */
    @NonCommitting
    public static @Nonnull Block getNotNull(@Nonnull SemanticType type, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return new Block(type, resultSet.getBytes(columnIndex));
    }
    
    @Override
    @NonCommitting
    @NonExposedRecipient
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        assert !isEncoding() : "This method is not called during encoding.";
        
        if (Database.getConfiguration().supportsBinaryStream()) {
            preparedStatement.setBinaryStream(parameterIndex, getInputStream(), getLength());
        } else {
            preparedStatement.setBytes(parameterIndex, getBytes());
        }
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given block.
     * 
     * @param block the block to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     * 
     * @require block == null || !block.isEncoding() : "The block is either null or not encoding.";
     */
    @NonCommitting
    public static void set(@Nullable Block block, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        assert block == null || !block.isEncoding(): "The block is either null or not encoding.";
        
        if (block == null) preparedStatement.setNull(parameterIndex, Types.BLOB);
        else block.set(preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    public @Nonnull Block clone() {
        return new Block(type, this);
    }
    
    @Pure
    @Override
    @NonExposedRecipient
    public @Nonnull String toString() {
        assert !isEncoding() : "This method is not called during encoding.";
        
        ensureEncoded();
        // See: 8.4.1. in http://www.postgresql.org/docs/9.0/static/datatype-binary.html
        final @Nonnull StringBuilder string = new StringBuilder("E'\\x");
        for (int i = offset; i < offset + length; i++) {
            if (string.length() > 4) string.append(" ");
            string.append(String.format("%02X", bytes[i]));
        }
        string.append("'");
        return string.toString();
    }
    
    
    /**
     * Encrypts this block with the given symmetric key.
     * 
     * @param type the semantic type of the encrypted block.
     * @param symmetricKey the symmetric key used for the encryption.
     * @param initializationVector the initialization vector for the encryption.
     * 
     * @return a new block containing the encryption of this block.
     */
    @Pure
    @NonExposedRecipient
    @Nonnull Block encrypt(@Nonnull SemanticType type, @Nonnull SymmetricKey symmetricKey, @Nonnull InitializationVector initializationVector) {
        assert !isEncoding() : "This method is not called during encoding.";
        
        ensureEncoded();
        assert bytes != null : "The byte array is allocated.";
        return new Block(type, symmetricKey.encrypt(initializationVector, bytes, offset, length));
    }
    
    /**
     * Decrypts this block with the given symmetric key.
     * 
     * @param type the semantic type of the decrypted block.
     * @param symmetricKey the symmetric key used for the decryption.
     * @param initializationVector the initialization vector for the decryption.
     * 
     * @return a new block containing the decryption of this block.
     */
    @Pure
    @NonExposedRecipient
    @Nonnull Block decrypt(@Nonnull SemanticType type, @Nonnull SymmetricKey symmetricKey, @Nonnull InitializationVector initializationVector) throws InvalidEncodingException {
        assert !isEncoding() : "This method is not called during encoding.";
        
        ensureEncoded();
        assert bytes != null : "The byte array is allocated.";
        return new Block(type, symmetricKey.decrypt(initializationVector, bytes, offset, length));
    }
    
    
    /**
     * Writes this block to the given output stream and optionally closes the output stream afterwards.
     * 
     * @param outputStream the output stream to writeTo to.
     * @param close whether the output stream shall be closed.
     */
    @NonExposedRecipient
    public void writeTo(@Nonnull OutputStream outputStream, boolean close) throws IOException {
        writeTo(0, outputStream, close);
    }
    
    /**
     * Writes this block from the given offset onwards to the given output stream and optionally closes the output stream afterwards.
     * 
     * @param offset the offset of the bytes to write.
     * @param outputStream the output stream to write to.
     * @param close whether the output stream shall be closed.
     */
    @NonExposedRecipient
    public void writeTo(@ValidIndex int offset, @Nonnull OutputStream outputStream, boolean close) throws IOException {
        writeTo(offset, getLength() - offset, outputStream, close);
    }
    
    /**
     * Writes this block from the given offset for the given length to the given output stream and optionally closes the output stream afterwards.
     * 
     * @param offset the offset of the bytes to write.
     * @param length the length of the bytes to write.
     * @param outputStream the output stream to write to.
     * @param close whether the output stream shall be closed.
     * 
     * @require offset >= 0 : "The offset is non-negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= getLength() : "The offset and length are within this block.";
     */
    @NonExposedRecipient
    public void writeTo(int offset, int length, @Nonnull OutputStream outputStream, boolean close) throws IOException {
        assert !isEncoding() : "This method is not called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        try {
            ensureEncoded();
            outputStream.write(bytes, this.offset + offset, length);
            outputStream.flush();
        } finally {
            if (close) outputStream.close();
        }
    }
    
    
    /**
     * Returns an input stream to read directly from this block.
     * 
     * @return an input stream to read directly from this block.
     * 
     * @require isNotEncoding() : "This method is not called during encoding.";
     */
    @Pure
    @NonExposedRecipient
    public @Nonnull InputStream getInputStream() {
        return getInputStream(0);
    }
    
    /**
     * Returns an input stream to read directly from this block at the given offset.
     * 
     * @param offset the offset of the returned input stream in this block.
     * 
     * @return an input stream to read directly from this block at the given offset.
     * 
     * @require isNotEncoding() : "This method is not called during encoding.";
     * @require offset >= 0 && offset < getLength() : "The offset is valid.";
     */
    @Pure
    @NonExposedRecipient
    public @Nonnull InputStream getInputStream(int offset) {
        return getInputStream(offset, getLength() - offset);
    }
    
    /**
     * Returns an input stream to read the given length directly from this block at the given offset.
     * 
     * @param offset the offset of the returned input stream in this block.
     * @param length the length of the returned input stream in this block.
     * 
     * @return an input stream to read the given length directly from this block at the given offset.
     * 
     * @require offset >= 0 : "The offset is non-negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= getLength() : "The offset and length are within this block.";
     */
    @Pure
    @NonExposedRecipient
    public @Nonnull InputStream getInputStream(int offset, int length) {
        assert !isEncoding() : "This method is not called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        ensureEncoded();
        return new BlockInputStream(this.offset + offset, length);
    }
    
    /**
     * This class allows to read directly from the byte array of a {@link Block}.
     * 
     * @author Kaspar Etter (kaspar.etter@digitalid.net)
     * @version 1.0
     */
    private final class BlockInputStream extends InputStream {
        
        /**
         * Stores the start position in the byte array.
         */
        private int start;
        
        /**
         * Stores the end position in the byte array.
         */
        private final int end;
        
        /**
         * Creates a new input stream with the given start and length in the byte array.
         * 
         * @param start the start position in the byte array.
         * @param length the length of this input stream.
         * 
         * @require bytes != null : "The byte array is not null.";
         * @require start >= 0 : "The start is non-negative.";
         * @require length > 0 : "The length is positive.";
         * @require start + length <= bytes.length : "The start and length are within the byte array.";
         */
        private BlockInputStream(int start, int length) {
            assert bytes != null : "The byte array is not null.";
            assert start >= 0 : "The start is non-negative.";
            assert length > 0 : "The length is positive.";
            assert start + length <= bytes.length : "The start and length are within the byte array.";
            
            this.start = start;
            this.end = start + length;
        }
        
        @Override
        public int read() throws IOException {
            if (start < end) {
                final byte result = bytes[start];
                start += 1;
                return result;
            } else {
                return -1;
            }
        }
        
        @Override
        @SuppressWarnings("AssignmentToMethodParameter")
        public int read(final @Nonnull byte[] bytes, final int offset, int length) throws IOException {
            if (length == 0) return 0;
            if (start == end) return -1;
            if (start + length > end) length = end - start;
            System.arraycopy(Block.this.bytes, start, bytes, offset, length);
            start += length;
            return length;
        }
        
        @Override
        @SuppressWarnings("AssignmentToMethodParameter")
        public long skip(long number) throws IOException {
            if (start + number > end) number = end - start;
            start += number;
            return number;
        }
        
        @Pure
        @Override
        public int available() throws IOException {
            return end - start;
        }
        
        @Override
        public void close() throws IOException {
            start = end;
        }
        
        
        /**
         * Stores the marked position.
         */
        private int mark = 0;
        
        @Override
        public void mark(int readAheadLimit) {
            mark = start;
        }
        
        @Override
        public void reset() throws IOException {
            start = mark;
        }
        
        @Pure
        @Override
        public boolean markSupported() {
            return true;
        }
        
    }
    
    
    /**
     * Returns an output stream to write directly into this block.
     * 
     * @return an output stream to write directly into this block.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     */
    @Pure
    @ExposedRecipient
    public @Nonnull OutputStream getOutputStream() {
        return getOutputStream(0);
    }
    
    /**
     * Returns an output stream to write directly into this block at the given offset.
     * 
     * @param offset the offset of the returned output stream in this block.
     * 
     * @return an output stream to write directly into this block at the given offset.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     * @require offset >= 0 && offset < getLength() : "The offset is valid.";
     */
    @Pure
    @ExposedRecipient
    public @Nonnull OutputStream getOutputStream(int offset) {
        return getOutputStream(offset, getLength() - offset);
    }
    
    /**
     * Returns an output stream to write the given length directly into this block at the given offset.
     * 
     * @param offset the offset of the returned output stream in this block.
     * @param length the length of the returned output stream in this block.
     * 
     * @return an output stream to write the given length directly into this block at the given offset.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     * @require offset >= 0 : "The offset is non-negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= getLength() : "The offset and length are within this block.";
     */
    @Pure
    @ExposedRecipient
    public @Nonnull OutputStream getOutputStream(int offset, int length) {
        assert isEncoding() : "This method may only be called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        return new BlockOutputStream(this.offset + offset, length);
    }
    
    /**
     * This class allows to stream directly into the byte array of a {@link Block}.
     * 
     * @author Kaspar Etter (kaspar.etter@digitalid.net)
     * @version 1.0
     */
    private final class BlockOutputStream extends OutputStream {
        
        /**
         * Stores the start position in the byte array.
         */
        private int start;
        
        /**
         * Stores the end position in the byte array.
         */
        private final int end;
        
        /**
         * Creates a new output stream with the given start and length in the byte array.
         * 
         * @param start the start position in the byte array.
         * @param length the length of this output stream.
         * 
         * @require bytes != null : "The byte array is not null.";
         * @require start >= 0 : "The start is non-negative.";
         * @require length > 0 : "The length is positive.";
         * @require start + length <= bytes.length : "The start and length are within the byte array.";
         */
        private BlockOutputStream(int start, int length) {
            assert bytes != null : "The byte array is not null.";
            assert start >= 0 : "The start is non-negative.";
            assert length > 0 : "The length is positive.";
            assert start + length <= bytes.length : "The start and length are within the byte array.";
            
            this.start = start;
            this.end = start + length;
        }
        
        @Override
        public void write(int b) throws IOException {
            if (start < length) {
                bytes[start] = (byte) b;
                start += 1;
            } else {
                throw new IOException("Could not write the byte as the end of the block has already been reached.");
            }
        }
        
        @Override
        public void write(@Nonnull byte[] bytes, int offset, int length) throws IOException {
            if (offset < 0 || length < 0 || offset + length > bytes.length) throw new IndexOutOfBoundsException();
            if (start + length <= end) {
                System.arraycopy(bytes, offset, Block.this.bytes, start, length);
                start += length;
            } else {
                throw new IOException("Could not write the bytes as the block is not big enough.");
            }
        }
        
        @Override
        public void close() throws IOException {
            start = length;
        }
        
    }
    
}
