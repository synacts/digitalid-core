package net.digitalid.core.block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.cryptography.InitializationVector;
import net.digitalid.utility.cryptography.SymmetricKey;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.MissingSupportException;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.size.NonEmpty;

import net.digitalid.core.block.annotations.annotations.Encoded;
import net.digitalid.core.block.annotations.annotations.Encoding;
import net.digitalid.core.block.annotations.annotations.EncodingRecipient;
import net.digitalid.core.block.annotations.annotations.NonEncoded;
import net.digitalid.core.block.annotations.annotations.NonEncoding;
import net.digitalid.core.block.annotations.annotations.NonEncodingRecipient;
import net.digitalid.core.identification.annotations.type.loaded.Loaded;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * A block is a sequence of bytes that is encoded according to some syntactic type.
 * In order to prevent unnecessary copying, this sequence is given by a byte array,
 * where an offset and a length is used to reference just a part of the array.
 * The bytes of a block can only be written when it is {@link #isEncoding() encoding}.
 * <p>
 * <em>Important:</em> Only share {@link #isEncoded() encoded} blocks between threads!
 * 
 * @invariant bytes != null || wrapper != null : "Either the byte array or the wrapper of this block is set.";
 * @invariant offset >= 0 : "The offset of this block in the byte array is non-negative.";
 * @invariant bytes == null || length > 0 : "If this block is allocated, its length is positive.";
 * @invariant bytes == null || offset + length <= bytes.length : "If this block is allocated, it may not exceed the byte array.";
 * @invariant !isEncoded() || isAllocated() : "If the block is encoded, it is also allocated.";
 * 
 * @deprecated This type is not required anymore since we're writing directly into the output stream.
 */
//@Immutable // TODO: not immutable. We do have impure methods.
@Deprecated
public final class Block implements Cloneable {
    
    /* -------------------------------------------------- Invariant -------------------------------------------------- */
    
    /**
     * Asserts that the class invariant still holds.
     */
    @Pure
    private boolean invariant() {
        Require.that(bytes != null || wrapper != null).orThrow("Either the byte array or the wrapper of this block is set.");
        Require.that(offset >= 0).orThrow("The offset of this block in the byte array is non-negative.");
        Require.that(bytes == null || length > 0).orThrow("If this block is allocated, its length is positive.");
        Require.that(bytes == null || offset + length <= bytes.length).orThrow("If this block is allocated, it may not exceed the byte array.");
        Require.that(!isEncoded() || isAllocated()).orThrow("If the block is encoded, it is also allocated.");
        return true;
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
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
    private @NonNegative int offset = 0;
    
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
    
    /* -------------------------------------------------- Eager Constructors -------------------------------------------------- */
    
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
        Require.that(type.isLoaded()).orThrow("The type declaration is loaded.");
        Require.that(bytes.length > 0).orThrow("The byte array is not empty.");
        Require.that(offset >= 0).orThrow("The offset is not negative.");
        Require.that(length > 0).orThrow("The length is positive.");
        Require.that(offset + length <= bytes.length).orThrow("The section fits into the given byte array.");
        
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
        Require.that(block.isEncoded()).orThrow("The given block is encoded.");
        
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
    
    /* -------------------------------------------------- Lazy Constructors -------------------------------------------------- */
    
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
        Require.that(type.isLoaded()).orThrow("The type declaration is loaded.");
        
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
    public static @Nonnull @NonEncoded @NonEncoding Block get(@Nonnull @Loaded SemanticType type, @Nonnull Wrapper<?> wrapper) {
        return new Block(type, wrapper);
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Returns the semantic type of this block.
     * <p>
     * <em>Important:</em> Do not rely on the identity of the type, but rather use
     * {@link SemanticType# isBasedOn(net.digitalid.service.core.identity.SemanticType)} for comparisons.
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
    @Impure
    public @Nonnull Block setType(@Nonnull @Loaded SemanticType type) {
        Require.that(type.isLoaded()).orThrow("The type declaration is loaded.");
        // TODO: figure out whether type check is actually required and useful.
//        Require.that(type.isBasedOn(getType())).orThrow("The type can only be downcast.");
        
        this.type = type;
        return this;
    }
    
    // TODO: Commented out, because the usefulness of this method is currently unclear. If it is required it should probably be implemented in the conversion.
//    /**
//     * Checks that the type of this block is based on the given type.
//     * 
//     * @param type the type to set for this block.
//     * 
//     * @return this block.
//     * 
//     * @throws InvalidBlockTypeException if this is not the case.
//     */
//    public @Nonnull Block checkType(@Nonnull @Loaded SemanticType type) throws InvalidBlockTypeException {
//        if (!this.type.isBasedOn(type)) { throw InvalidBlockTypeException.get(type, this.type); }
//        return this;
//    }
    
    /* -------------------------------------------------- Get Bytes -------------------------------------------------- */
    
    /**
     * Returns the byte at the given index of this block.
     * 
     * @param index the index of the byte to be returned.
     * 
     * @return the byte at the given index of this block.
     * 
     * @ensure isEncoded() : "This block is encoded.";
     */
    @Pure
    @NonEncodingRecipient
    public byte getByte(int index) {
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        Require.that(index >= 0 && index < getLength()).orThrow("The index is valid.");
        
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
    @Pure
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
    @Pure
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
    @Pure
    @NonEncodingRecipient
    public @Capturable @Nonnull byte[] getBytes(@NonNegative int offset, @NonNegative int length) {
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        Require.that(offset >= 0).orThrow("The offset is non-negative.");
        Require.that(length >= 0).orThrow("The length is non-negative.");
        Require.that(offset + length <= getLength()).orThrow("The offset and length are within this block.");
        
        encodeIfNotYetEncoded();
        final @Nonnull byte[] result = new byte[length];
        System.arraycopy(bytes, this.offset + offset, result, 0, length);
        return result;
    }
    
    /* -------------------------------------------------- Set Bytes -------------------------------------------------- */
    
    /**
     * Sets the byte at the given index of this block to the given value.
     * 
     * @param index the index of the byte to be set.
     * @param value the new value of the byte at the given index.
     */
    @Impure
    @EncodingRecipient
    public void setByte(int index, byte value) {
        Require.that(isEncoding()).orThrow("This method may only be called during encoding.");
        Require.that(index >= 0 && index < getLength()).orThrow("The index is valid.");
        
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
    @Impure
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
    @Impure
    @EncodingRecipient
    public void setBytes(@NonNegative int index, @Nonnull byte[] values, @NonNegative int offset, @NonNegative int length) {
        Require.that(isEncoding()).orThrow("This method may only be called during encoding.");
        Require.that(index >= 0).orThrow("The index is non-negative.");
        Require.that(index + length <= getLength()).orThrow("The given values may not exceed this block.");
        Require.that(offset >= 0).orThrow("The offset is non-negative.");
        Require.that(length >= 0).orThrow("The length is non-negative.");
        Require.that(offset + length <= values.length).orThrow("The indicated section may not exceed the given byte array.");
        
        System.arraycopy(values, offset, bytes, this.offset + index, length);
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Decodes the value in this block.
     * 
     * @return the value encoded in this block.
     */
    @Pure
    @NonEncodingRecipient
    public long decodeValue() {
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        
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
    @Impure
    @EncodingRecipient
    public void encodeValue(long value) {
        Require.that(isEncoding()).orThrow("This method may only be called during encoding.");
        
        long shifter = value;
        for (int i = length - 1; i >= 0; i--) {
            bytes[offset + i] = (byte) shifter;
            shifter >>>= 8;
        }
    }
    
    /* -------------------------------------------------- Length -------------------------------------------------- */
    
    /**
     * Returns the length of this block.
     * 
     * @return the length of this block.
     */
    @Pure
    public @Positive int getLength() {
        if (length < 0) {
            Require.that(wrapper != null).orThrow("The length may only be negative in case of lazy encoding.");
            length = wrapper.determineLength();
            
            Require.that(length > 0).orThrow("The length is positive (i.e. the block is not empty).");
            assert invariant();
        }
        return length;
    }
    
    /* -------------------------------------------------- Encoding -------------------------------------------------- */
    
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
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        
        if (!isEncoded()) { encode(); }
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
        Require.that(!isAllocated()).orThrow("This block may not yet be allocated.");
        
        bytes = new byte[getLength()];
        Require.that(wrapper != null).orThrow("The byte array may only be null in case of lazy encoding.");
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
    @Impure
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
    @Impure
    @NonEncodingRecipient
    public void writeTo(@Nonnull @Encoding Block block, @NonNegative int offset, @Positive int length) {
        Require.that(block.isEncoding()).orThrow("The given block is in the process of being encoded.");
        Require.that(offset >= 0).orThrow("The offset is not negative.");
        Require.that(length > 0).orThrow("The length is positive.");
        Require.that(offset + length <= block.getLength()).orThrow("The indicated section may not exceed the given block.");
        Require.that(getLength() == length).orThrow("This block has to have the same length as the designated section.");
        Require.that(!isEncoding()).orThrow("This block is not in the process of being encoded.");
        
        if (isEncoded()) {
            System.arraycopy(this.bytes, this.offset, block.bytes, block.offset + offset, length);
        } else {
            this.bytes = block.bytes;
            this.offset = block.offset + offset;
            this.length = length;
            Require.that(wrapper != null).orThrow("The byte array may only be null in case of lazy encoding.");
            wrapper.encode(this);
            this.encoded = true;
        }
        
        assert invariant();
    }
    
    /* -------------------------------------------------- Hash -------------------------------------------------- */
    
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
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        
        encodeIfNotYetEncoded();
        try {
            final @Nonnull MessageDigest instance = MessageDigest.getInstance("SHA-256");
            instance.update(bytes, offset, length);
            return new BigInteger(1, instance.digest());
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw MissingSupportException.with("The hashing algorithm 'SHA-256' is not supported on this platform.", exception);
        }
    }
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @Encoded Block clone() {
        return Block.get(type, this);
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    @NonEncodingRecipient
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof Block)) { return false; }
        final @Nonnull Block other = (Block) object;
        
        if (!getType().equals(other.getType())) { return false; }
        
        this.encodeIfNotYetEncoded();
        other.encodeIfNotYetEncoded();
        
        if (this.length != other.length) { return false; }
        
        for (int i = 0; i < length; i++) {
            if (this.bytes[this.offset + i] != other.bytes[other.offset + i]) { return false; }
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
        Require.that(offset >= 0).orThrow("The offset is non-negative.");
        Require.that(length >= 0).orThrow("The length is non-negative.");
        Require.that(offset + length <= bytes.length).orThrow("The section fits into the given byte array.");
        
        // See: 8.4.1. in http://www.postgresql.org/docs/9.0/static/datatype-binary.html
        final @Nonnull StringBuilder string = new StringBuilder("E'\\x");
        for (int i = offset; i < offset + length; i++) {
            // if (string.length() > 4) { string.append(" "); }
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
        Require.that(bytes != null).orThrow("Encoded now.");
        return toString(bytes, offset, length);
    }
    
//    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
//    
//    /**
//     * The XDF converter for this class.
//     */
//    @Immutable
//    public static class XDFConverter extends NonRequestingXDFConverter<Block, Object> {
//        
//        /**
//         * Creates a new XDF converter with the given type.
//         * 
//         * @param type the type of the blocks which are returned.
//         */
//        private XDFConverter(@Nonnull @Loaded SemanticType type) {
//            super(type);
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Block encodeNonNullable(@Nonnull Block block) {
//            return block;
//        }
//        
//        @Pure
//        @Override
//        public @Nonnull Block decodeNonNullable(@Nonnull Object none, @Nonnull Block block) {
//            return block;
//        }
//        
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull XDFConverter getXDFConverter() {
//        return new XDFConverter(type);
//    }
//    
//    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
//    
//    /**
//     * Stores the declaration of this class.
//     */
//    public static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("block", SQLType.BINARY);
//    
//    /**
//     * Stores the SQL converter which is used to store and restore blocks.
//     */
//    public static final @Nonnull SQLConverter<Block, SemanticType> SQL_CONVERTER = new SQLConverter<Block, SemanticType>(DECLARATION) {
//        
//        @Override
//        public void storeNonNullable(@Nonnull Block block, @NonCaptured @Nonnull ValueCollector collector) throws FailedValueStoringException {
//            if (Database.getInstance().supportsBinaryStreams()) {
//                collector.setBinaryStream(block.getInputStream(), block.getLength());
//            } else {
//                collector.setBinary(block.getBytes());
//            }
//        }
//        
//        @Override
//        public @Nullable Block restoreNullable(@Nonnull SemanticType type, @NonCaptured @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptValueException, InternalException {
//            final @Nullable byte[] bytes = result.getBinary();
//            return bytes == null ? null : Block.get(type, bytes);
//        }
//        
//    };
//    
//    @Pure
//    @Override
//    public @Nonnull SQLConverter<Block, SemanticType> getSQLConverter() {
//        return SQL_CONVERTER;
//    }
    
    /* -------------------------------------------------- Cryptography -------------------------------------------------- */
    
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
    public @Nonnull Block encrypt(@Nonnull SemanticType type, @Nonnull SymmetricKey symmetricKey, @Nonnull InitializationVector initializationVector) {
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        
        encodeIfNotYetEncoded();
        Require.that(bytes != null).orThrow("The byte array is allocated.");
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
    public @Nonnull Block decrypt(@Nonnull SemanticType type, @Nonnull SymmetricKey symmetricKey, @Nonnull InitializationVector initializationVector) throws InternalException {
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        
        encodeIfNotYetEncoded();
        Require.that(bytes != null).orThrow("The byte array is allocated.");
        return Block.get(type, symmetricKey.decrypt(initializationVector, bytes, offset, length));
    }
    
    /* -------------------------------------------------- Write -------------------------------------------------- */
    
    /**
     * Writes this block to the given output stream and optionally closes the output stream afterwards.
     * 
     * @param outputStream the output stream to writeTo to.
     * @param close whether the output stream shall be closed.
     */
    @Impure
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
    @Impure
    @NonEncodingRecipient
    public void writeTo(int offset, @Nonnull OutputStream outputStream, boolean close) throws IOException {
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
    @Impure
    @NonEncodingRecipient
    public void writeTo(int offset, int length, @Nonnull OutputStream outputStream, boolean close) throws IOException {
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        Require.that(offset >= 0).orThrow("The offset is non-negative.");
        Require.that(length > 0).orThrow("The length is positive.");
        Require.that(offset + length <= getLength()).orThrow("The offset and length are within this block.");
        
        try {
            encodeIfNotYetEncoded();
            outputStream.write(bytes, this.offset + offset, length);
            outputStream.flush();
        } finally {
            if (close) { outputStream.close(); }
        }
    }
    
    /* -------------------------------------------------- Input Stream -------------------------------------------------- */
    
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
        Require.that(!isEncoding()).orThrow("This method is not called during encoding.");
        Require.that(offset >= 0).orThrow("The offset is non-negative.");
        Require.that(length > 0).orThrow("The length is positive.");
        Require.that(offset + length <= getLength()).orThrow("The offset and length are within this block.");
        
        encodeIfNotYetEncoded();
        return new BlockInputStream(this.offset + offset, length);
    }
    
    /**
     * This class allows to read directly from the byte array of a {@link Block}.
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
            Require.that(bytes != null).orThrow("The byte array is not null.");
            Require.that(start >= 0).orThrow("The start is non-negative.");
            Require.that(length > 0).orThrow("The length is positive.");
            Require.that(start + length <= bytes.length).orThrow("The start and length are within the byte array.");
            
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
            if (length == 0) { return 0; }
            if (start == end) { return -1; }
            if (start + length > end) { length = end - start; }
            System.arraycopy(Block.this.bytes, start, bytes, offset, length);
            start += length;
            return length;
        }
        
        @Override
        @SuppressWarnings("AssignmentToMethodParameter")
        public long skip(long number) throws IOException {
            if (start + number > end) { number = end - start; }
            start += number;
            return number;
        }
        
        @Pure
        @Override
        public int available() throws IOException {
            return end - start;
        }
    
        @Impure
        @Override
        public void close() throws IOException {
            start = end;
        }
        
        
        /**
         * Stores the marked position.
         */
        private int mark = 0;
    
        @Impure
        @Override
        public void mark(int readAheadLimit) {
            mark = start;
        }
    
        @Impure
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
    
    /* -------------------------------------------------- Output Stream -------------------------------------------------- */
    
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
        Require.that(isEncoding()).orThrow("This method may only be called during encoding.");
        Require.that(offset >= 0).orThrow("The offset is non-negative.");
        Require.that(length > 0).orThrow("The length is positive.");
        Require.that(offset + length <= getLength()).orThrow("The offset and length are within this block.");
        
        return new BlockOutputStream(this.offset + offset, length);
    }
    
    /**
     * This class allows to stream directly into the byte array of a {@link Block}.
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
            Require.that(bytes != null).orThrow("The byte array is not null.");
            Require.that(start >= 0).orThrow("The start is non-negative.");
            Require.that(length > 0).orThrow("The length is positive.");
            Require.that(start + length <= bytes.length).orThrow("The start and length are within the byte array.");
            
            this.start = start;
            this.end = start + length;
        }
    
        @Impure
        @Override
        public void write(int b) throws IOException {
            if (start < length) {
                bytes[start] = (byte) b;
                start += 1;
            } else {
                throw new IOException("Could not write the byte as the end of the block has already been reached.");
            }
        }
    
        @Impure
        @Override
        public void write(@Nonnull byte[] bytes, int offset, int length) throws IOException {
            if (offset < 0 || length < 0 || offset + length > bytes.length) { throw new IndexOutOfBoundsException(); }
            if (start + length <= end) {
                System.arraycopy(bytes, offset, Block.this.bytes, start, length);
                start += length;
            } else {
                throw new IOException("Could not write the bytes as the block is not big enough.");
            }
        }
    
        @Impure
        @Override
        public void close() throws IOException {
            start = length;
        }
        
    }
    
}
