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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Captured;
import net.digitalid.core.annotations.Encoded;
import net.digitalid.core.annotations.Encoding;
import net.digitalid.core.annotations.EncodingRecipient;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.NonEncoded;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonEncodingRecipient;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNegative;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Positive;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.ValidIndex;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.cryptography.InitializationVector;
import net.digitalid.core.cryptography.SymmetricKey;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.Database;
import net.digitalid.core.database.SQLType;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.storable.NonConceptFactory;
import net.digitalid.core.storable.SimpleNonConceptFactory;
import net.digitalid.core.storable.Storable;

/**
 * A block is a sequence of bytes that is encoded according to some syntactic type.
 * In order to prevent unnecessary copying, this sequence is given by a byte array,
 * where an offset and a length is used to reference just a part of the array.
 * If a block is annotated as {@link Encoding exposed}, it {@link #isEncoding() is encoding}.
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
public final class Block implements Storable<Block>, Cloneable {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Conversions –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the given non-nullable storable as a block.
     * 
     * @param storable the non-nullable object to convert.
     * 
     * @return the given non-nullable storable as a block.
     */
    @Pure
    public static @Nonnull <V extends Storable<V>> Block fromNonNullable(@Nonnull V storable) {
        return storable.getFactory().encodeNonNullable(storable);
    }
    
    /**
     * Returns the given nullable storable as a block.
     * 
     * @param storable the nullable object to convert.
     * 
     * @return the given nullable storable as a block.
     */
    @Pure
    public static @Nullable <V extends Storable<V>> Block fromNullable(@Nullable V storable) {
        return storable == null ? null : fromNonNullable(storable);
    }
    
    /**
     * Returns the given non-nullable storable as a block of the given type.
     * 
     * @param storable the non-nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given non-nullable storable as a block of the given type.
     * 
     * @require type.isBasedOn(storable.getFactory().getType()) : "The given type is based on its type.";
     */
    @Pure
    public static @Nonnull <V extends Storable<V>> Block fromNonNullable(@Nonnull V storable, @Nonnull @Loaded SemanticType type) {
        return fromNonNullable(storable).setType(type);
    }
    
    /**
     * Returns the given nullable storable as a block of the given type.
     * 
     * @param storable the nullable object to be converted to a block.
     * @param type the type which is to be set for the returned block.
     * 
     * @return the given nullable storable as a block of the given type.
     * 
     * @require storable == null || type.isBasedOn(storable.getFactory().getType()) : "If the storable instance is not null, the given type is based on its type.";
     */
    @Pure
    public static @Nullable <V extends Storable<V>> Block fromNullable(@Nullable V storable, @Nonnull @Loaded SemanticType type) {
        return storable == null ? null : fromNonNullable(storable).setType(type);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Invariant –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Fields –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type of this block.
     * 
     * @invariant new.isBasedOn(old) : "The type can only be downcast.";
     */
    private @Nonnull @Loaded SemanticType type;
    
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
     * Stores the given or determined length of this block.
     * A negative value indicates that the length of this block has not yet been determined.
     */
    private int length = -1;
    
    /**
     * Stores the wrapper of this block for lazy encoding or null otherwise.
     */
    private final @Nullable Wrapper<?> wrapper;
    
    /**
     * Stores whether this block is already encoded and can thus no longer be written to.
     */
    private boolean encoded = false;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructors –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new block with the given parameters.
     * 
     * @param type the semantic type of the new block.
     * @param bytes the byte array of the new block.
     * @param offset the offset of the new block in the given byte array.
     * @param length the length of the new block.
     * 
     * @require offset + length <= bytes.length : "The section fits into the given byte array.";
     */
    private @Encoded Block(@Nonnull @Loaded SemanticType type, @Captured @Nonnull @NonEmpty byte[] bytes, @NonNegative int offset, @Positive int length) {
        assert type.isLoaded() : "The type declaration is loaded.";
        assert bytes.length > 0 : "The byte array is not empty.";
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= bytes.length : "The section fits into the given byte array.";
        
        this.type = type;
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        this.wrapper = null;
        this.encoded = true;
        
        assert invariant();
    }
    
    /**
     * Allocates a new block of the given type with the given byte array.
     * 
     * @param type the semantic type of the new block.
     * @param bytes the byte array of the new block.
     * 
     * @return a new block of the given type with the given byte array.
     */
    @Pure
    public static @Nonnull @Encoded Block get(@Nonnull @Loaded SemanticType type, @Captured @Nonnull @NonEmpty byte[] bytes) {
        return new Block(type, bytes, 0, bytes.length);
    }
    
    /**
     * Allocates a new block with the indicated section in the given block.
     * 
     * @param type the semantic type of the new block.
     * @param block the block containing the byte array.
     * @param offset the offset of the new block in the given block.
     * @param length the length of the new block.
     * 
     * @return a new block with the indicated section in the given block.
     * 
     * @require offset + length <= block.getLength() : "The section fits into the given block.";
     */
    @Pure
    public static @Nonnull @Encoded Block get(@Nonnull @Loaded SemanticType type, @Nonnull @Encoded Block block, @NonNegative int offset, @Positive int length) {
        assert block.isEncoded() : "The given block is encoded.";
        
        return new Block(type, block.bytes, block.offset + offset, length);
    }
    
    /**
     * Allocates a new block of the given type based on the given block.
     * This constructor allows syntactic types to recast a block for their internal decoding.
     * 
     * @param type the semantic type of the new block.
     * @param block the block containing the byte array.
     * 
     * @return a new block of the given type based on the given block.
     */
    @Pure
    public static @Nonnull @Encoded Block get(@Nonnull SemanticType type, @Nonnull @NonEncoding Block block) {
        return Block.get(type, block.encodeIfNotYetEncoded(), 0, block.getLength());
    }
    
    /**
     * Allocates a new block of the given type with the given wrapper.
     * This constructor is used for the lazy encoding of blocks.
     * 
     * @param type the semantic type of the new block.
     * @param wrapper the wrapper of the new block.
     * 
     * @ensure !isAllocated() : "This block is not yet allocated.";
     */
    private @NonEncoded @NonEncoding Block(@Nonnull @Loaded SemanticType type, @Nonnull Wrapper<?> wrapper) {
        assert type.isLoaded() : "The type declaration is loaded.";
        
        this.type = type;
        this.wrapper = wrapper;
        
        assert invariant();
    }
    
    /**
     * Allocates a new block of the given type with the given wrapper.
     * This constructor is used for the lazy encoding of blocks.
     * 
     * @param type the semantic type of the new block.
     * @param wrapper the wrapper of the new block.
     * 
     * @return a new block of the given type with the given wrapper.
     * 
     * @ensure !isAllocated() : "This block is not yet allocated.";
     */
    @Pure
    static @Nonnull @NonEncoded @NonEncoding Block get(@Nonnull @Loaded SemanticType type, @Nonnull Wrapper<?> wrapper) {
        return new Block(type, wrapper);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the semantic type of this block.
     * <p>
     * <em>Important:</em> Do not rely on the identity of the type, but rather use
     * {@link SemanticType#isBasedOn(net.digitalid.core.identity.SemanticType)} for comparisons.
     * 
     * @return the semantic type of this block.
     * 
     * @ensure new.isBasedOn(old) : "The type can only be downcast.";
     */
    @Pure
    public @Nonnull @Loaded SemanticType getType() {
        return type;
    }
    
    /**
     * Sets the semantic type of this block.
     * 
     * @param type the type to set for this block.
     * 
     * @return this block.
     * 
     * @require type.isBasedOn(getType()) : "The type can only be downcast.";
     */
    public @Nonnull Block setType(@Nonnull @Loaded SemanticType type) {
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
     */
    public @Nonnull Block checkType(@Nonnull @Loaded SemanticType type) throws InvalidEncodingException {
        if (!this.type.isBasedOn(type)) throw new InvalidEncodingException("The type of this block (" + this.type.getAddress() + ") is not based on the given type (" + type.getAddress() + ").");
        return this;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Get Bytes –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the byte at the given index of this block.
     * 
     * @param index the index of the byte to be returned.
     * 
     * @return the byte at the given index of this block.
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    @NonEncodingRecipient
    public @Nonnull byte getByte(@ValidIndex int index) {
        assert !isEncoding() : "This method is not called during encoding.";
        assert index >= 0 && index < getLength() : "The index is valid.";
        
        encodeIfNotYetEncoded();
        return bytes[offset + index];
    }
    
    /**
     * Returns the bytes of this block.
     * Use {@link #getInputStream()} if possible to avoid unnecessary copying.
     * 
     * @return the bytes of this block.
     * 
     * @ensure isEncoded() : "The block is encoded.";
     * @ensure result.length == getLength() : "The result has the whole length.";
     */
    @NonEncodingRecipient
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
    @NonEncodingRecipient
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
     * @require offset + length <= getLength() : "The offset and length are within this block.";
     * 
     * @ensure isEncoded() : "The block is encoded.";
     * @ensure result.length == length : "The result has the given length.";
     */
    @NonEncodingRecipient
    public @Capturable @Nonnull byte[] getBytes(@NonNegative int offset, @NonNegative int length) {
        assert !isEncoding() : "This method is not called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length >= 0 : "The length is non-negative.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        encodeIfNotYetEncoded();
        final @Nonnull byte[] result = new byte[length];
        System.arraycopy(bytes, this.offset + offset, result, 0, length);
        return result;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Set Bytes –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Sets the byte at the given index of this block to the given value.
     * 
     * @param index the index of the byte to be set.
     * @param value the new value of the byte at the given index.
     */
    @EncodingRecipient
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
     * @require offset + values.length <= getLength() : "The given values may not exceed this block.";
     */
    @EncodingRecipient
    public void setBytes(@NonNegative int offset, @Nonnull byte[] values) {
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
     * @require index + length <= getLength() : "The given values may not exceed this block.";
     * @require offset + length <= values.length : "The indicated section may not exceed the given byte array.";
     */
    @EncodingRecipient
    public void setBytes(@NonNegative int index, @Nonnull byte[] values, @NonNegative int offset, @NonNegative int length) {
        assert isEncoding() : "This method may only be called during encoding.";
        assert index >= 0 : "The index is not negative.";
        assert index + length <= getLength() : "The given values may not exceed this block.";
        assert offset >= 0 : "The offset is not negative.";
        assert length >= 0 : "The length is non-negative.";
        assert offset + length <= values.length : "The indicated section may not exceed the given byte array.";
        
        System.arraycopy(values, offset, bytes, this.offset + index, length);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Value –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the value in this block.
     * 
     * @return the value encoded in this block.
     */
    @Pure
    @NonEncodingRecipient
    public long decodeValue() {
        assert !isEncoding() : "This method is not called during encoding.";
        
        encodeIfNotYetEncoded();
        
        long value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (bytes[offset + i] & 0xff);
        }
        return value;
    }
    
    /**
     * Encodes the given value into this block.
     * 
     * @param value the value to be encoded.
     */
    @EncodingRecipient
    public void encodeValue(long value) {
        assert isEncoding() : "This method may only be called during encoding.";
        
        long shifter = value;
        for (int i = length - 1; i >= 0; i--) {
            bytes[offset + i] = (byte) shifter;
            shifter >>>= 8;
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Length –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the length of this block.
     * 
     * @return the length of this block.
     */
    @Pure
    public @Positive int getLength() {
        if (length < 0) {
            assert wrapper != null : "The length may only be negative in case of lazy encoding.";
            length = wrapper.determineLength();
            
            assert length > 0 : "The length is positive (i.e. the block is not empty).";
            assert invariant();
        }
        return length;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
     * <em>Important:</em> If a parameter or local variable is not annotated as {@link Encoding encoding}, it is assumed that such a block is <em>not</em> in the process of being encoded.
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
     * @ensure isEncoded() : "This block is encoded.";
     */
    @Pure
    @NonEncodingRecipient
    public @Nonnull @Encoded Block encodeIfNotYetEncoded() {
        assert !isEncoding() : "This method is not called during encoding.";
        
        if (!isEncoded()) encode();
        return this;
    }
    
    /**
     * Encodes the byte array of this block.
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    @Pure
    @NonEncodingRecipient
    private void encode() {
        assert !isAllocated() : "This block may not yet be allocated.";
        
        bytes = new byte[getLength()];
        assert wrapper != null : "The byte array may only be null in case of lazy encoding.";
        wrapper.encode(this);
        encoded = true;
        
        assert invariant();
    }
    
    /**
     * Writes this block into the given block (and encodes this block in place if necessary).
     * This method allows syntactic types to recast a block for their internal encoding.
     * 
     * @param block the block to writeTo this block to.
     * 
     * @require getLength() == block.getLength() : "This block has to have the same length as the given block.";
     */
    @NonEncodingRecipient
    public void writeTo(@Nonnull @Encoding Block block) {
        writeTo(block, 0, block.getLength());
    }
    
    /**
     * Writes this block into the designated section of the given block (and encodes this block in place if necessary).
     * 
     * @param block the block containing the designated section.
     * @param offset the offset of the section in the given block.
     * @param length the length of the section in the given block.
     * 
     * @require offset + length <= block.getLength() : "The indicated section may not exceed the given block.";
     * @require getLength() == length : "This block has to have the same length as the designated section.";
     */
    @NonEncodingRecipient
    public void writeTo(@Nonnull @Encoding Block block, @NonNegative int offset, @Positive int length) {
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Hash –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the SHA-256 hash of this block.
     * 
     * @return the SHA-256 hash of this block.
     * 
     * @ensure isEncoded() : "This block is encoded.";
     * @ensure return.bitLength() <= Parameters.HASH : "The length of the result is at most Parameters.HASH.";
     */
    @Pure
    @NonEncodingRecipient
    public @Nonnull @NonNegative BigInteger getHash() {
        assert !isEncoding() : "This method is not called during encoding.";
        
        encodeIfNotYetEncoded();
        try {
            final @Nonnull MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(bytes, offset, length);
            return new BigInteger(1, instance.digest());
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw new ShouldNeverHappenError("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cloneable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull @Encoded Block clone() {
        return Block.get(type, this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonEncodingRecipient
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Block)) return false;
        final @Nonnull Block other = (Block) object;
        
        if (!getType().equals(other.getType())) return false;
        
        this.encodeIfNotYetEncoded();
        other.encodeIfNotYetEncoded();
        
        if (this.length != other.length) return false;
        
        for (int i = 0; i < length; i++) {
            if (this.bytes[this.offset + i] != other.bytes[other.offset + i]) return false;
        }
        
        return true;
    }
    
    @Pure
    @Override
    @NonEncodingRecipient
    public int hashCode() {
        encodeIfNotYetEncoded();
        int result = 1;
        for (int i = 0; i < length; i++) {
            result = 31 * result + bytes[offset + i];
        }
        return result;
    }
    
    /**
     * Returns the given section in the byte array as a string.
     * 
     * @param bytes the byte array which contains the section.
     * @param offset the offset of the section in the byte array.
     * @param length the length of the section in the byte array.
     * 
     * @return the given section in the byte array as a string.
     * 
     * @require offset >= 0 : "The offset is non-negative.";
     * @require length >= 0 : "The length is non-negative.";
     * @require offset + length <= bytes.length : "The section fits into the given byte array.";
     */
    @Pure
    public static @Nonnull String toString(@Nonnull byte[] bytes, @NonNegative int offset, @NonNegative int length) {
        assert offset >= 0 : "The offset is non-negative.";
        assert length >= 0 : "The length is non-negative.";
        assert offset + length <= bytes.length : "The section fits into the given byte array.";
        
        // See: 8.4.1. in http://www.postgresql.org/docs/9.0/static/datatype-binary.html
        final @Nonnull StringBuilder string = new StringBuilder("E'\\x");
        for (int i = offset; i < offset + length; i++) {
            // if (string.length() > 4) string.append(" ");
            string.append(String.format("%02X", bytes[i]));
        }
        return string.append("'").toString();
    }
    
    /**
     * Returns the given byte array as a string.
     * 
     * @param bytes the byte array to encode.
     * 
     * @return the given byte array as a string.
     */
    @Pure
    public static @Nonnull String toString(@Nonnull byte[] bytes) {
        return toString(bytes, 0, bytes.length);
    }
    
    @Pure
    @Override
    @NonEncodingRecipient
    public @Nonnull String toString() {
        encodeIfNotYetEncoded();
        assert bytes != null : "Encoded now.";
        return toString(bytes, offset, length);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for blocks.
     */
    private static class Factory extends SimpleNonConceptFactory<Block> {
        
        /**
         * Stores the column for blocks.
         */
        private static final @Nonnull Column column = Column.get("block", SQLType.BLOB);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the type of the blocks which are returned.
         */
        private Factory(@Nonnull @Loaded SemanticType type) {
            super(type, column);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull Block block) {
            return block;
        }
        
        @Pure
        @Override
        public @Nonnull Block decodeNonNullable(@Nonnull Block block) {
            return block;
        }
        
        @Pure
        @Override
        protected @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull Block block) {
            return FreezableArray.getNonNullable(block.toString());
        }
        
        @Override
        public void setNonNullable(@Nonnull Block block, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            if (Database.getConfiguration().supportsBinaryStream()) {
                preparedStatement.setBinaryStream(parameterIndex, block.getInputStream(), block.getLength());
            } else {
                preparedStatement.setBytes(parameterIndex, block.getBytes());
            }
        }
        
        @Pure
        @Override
        public @Nullable Block getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nonnull byte[] bytes = resultSet.getBytes(columnIndex);
            if (resultSet.wasNull()) return null;
            else return Block.get(getType(), bytes);
        }
        
    }
    
    @Pure
    @Override
    public @Nonnull NonConceptFactory<Block> getFactory() {
        return new Factory(type);
    }
    
    /**
     * Returns a new factory for the given type.
     * 
     * @param type the type of the blocks which are returned by the factory.
     * 
     * @return a new factory for the given type.
     */
    @Pure
    public static @Nonnull NonConceptFactory<Block> getFactory(@Nonnull @Loaded SemanticType type) {
        return new Factory(type);
    }
    
    /**
     * Stores the factory for blocks of unknown type.
     */
    public static final @Nonnull NonConceptFactory<Block> FACTORY = new Factory(SemanticType.UNKNOWN);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Cryptography –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    @NonEncodingRecipient
    @Nonnull Block encrypt(@Nonnull SemanticType type, @Nonnull SymmetricKey symmetricKey, @Nonnull InitializationVector initializationVector) {
        assert !isEncoding() : "This method is not called during encoding.";
        
        encodeIfNotYetEncoded();
        assert bytes != null : "The byte array is allocated.";
        return Block.get(type, symmetricKey.encrypt(initializationVector, bytes, offset, length));
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
    @NonEncodingRecipient
    @Nonnull Block decrypt(@Nonnull SemanticType type, @Nonnull SymmetricKey symmetricKey, @Nonnull InitializationVector initializationVector) throws InvalidEncodingException {
        assert !isEncoding() : "This method is not called during encoding.";
        
        encodeIfNotYetEncoded();
        assert bytes != null : "The byte array is allocated.";
        return Block.get(type, symmetricKey.decrypt(initializationVector, bytes, offset, length));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Write –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Writes this block to the given output stream and optionally closes the output stream afterwards.
     * 
     * @param outputStream the output stream to writeTo to.
     * @param close whether the output stream shall be closed.
     */
    @NonEncodingRecipient
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
    @NonEncodingRecipient
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
    @NonEncodingRecipient
    public void writeTo(int offset, int length, @Nonnull OutputStream outputStream, boolean close) throws IOException {
        assert !isEncoding() : "This method is not called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        try {
            encodeIfNotYetEncoded();
            outputStream.write(bytes, this.offset + offset, length);
            outputStream.flush();
        } finally {
            if (close) outputStream.close();
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Input Stream –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns an input stream to read directly from this block.
     * 
     * @return an input stream to read directly from this block.
     * 
     * @require isNotEncoding() : "This method is not called during encoding.";
     */
    @Pure
    @NonEncodingRecipient
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
    @NonEncodingRecipient
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
    @NonEncodingRecipient
    public @Nonnull InputStream getInputStream(int offset, int length) {
        assert !isEncoding() : "This method is not called during encoding.";
        assert offset >= 0 : "The offset is non-negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= getLength() : "The offset and length are within this block.";
        
        encodeIfNotYetEncoded();
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Output Stream –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns an output stream to write directly into this block.
     * 
     * @return an output stream to write directly into this block.
     * 
     * @require isEncoding() : "This method may only be called during encoding.";
     */
    @Pure
    @EncodingRecipient
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
    @EncodingRecipient
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
    @EncodingRecipient
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
