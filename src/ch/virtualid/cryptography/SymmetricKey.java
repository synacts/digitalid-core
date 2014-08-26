package ch.virtualid.cryptography;

import ch.virtualid.annotations.Capturable;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Symmetric keys are used to encrypt and decrypt byte arrays with the Advanced Encryption Standard (AES).
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class SymmetricKey implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code symmetric.key@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("symmetric.key@virtualid.ch").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the length of symmetric keys in bytes.
     */
    private static final int LENGTH = Parameters.ENCRYPTION_KEY / 8;
    
    /**
     * Stores the mode of the encryption cipher.
     */
    private static final @Nonnull String mode = "AES/CBC/PKCS5Padding";
    
    
    /**
     * Stores the value of this symmetric key.
     */
    private final @Nonnull BigInteger value;
    
    /**
     * Stores the key of this symmetric key.
     */
    private final @Nonnull Key key;
    
    /**
     * Creates a new symmetric key with a random value.
     */
    public SymmetricKey() {
        this(new BigInteger(Parameters.ENCRYPTION_KEY, new SecureRandom()));
    }
    
    /**
     * Creates a new symmetric key with the given value.
     * 
     * @param value the value of the new symmetric key.
     */
    public SymmetricKey(@Nonnull BigInteger value) {
        this.value = value;
        final @Nonnull byte[] bytes = value.toByteArray();
        final @Nonnull byte[] key = new byte[LENGTH];
        System.arraycopy(bytes, Math.max(bytes.length - LENGTH, 0), key, Math.max(LENGTH - bytes.length, 0), Math.min(LENGTH, bytes.length));
        this.key = new SecretKeySpec(key, "AES");
    }
    
    /**
     * Creates a new time from the given block.
     * 
     * @param block the block containing the time.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    public SymmetricKey(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        value = new IntegerWrapper(block).getValue();
        final @Nonnull byte[] bytes = value.toByteArray();
        final @Nonnull byte[] key = new byte[LENGTH];
        System.arraycopy(bytes, Math.max(bytes.length - LENGTH, 0), key, Math.max(LENGTH - bytes.length, 0), Math.min(LENGTH, bytes.length));
        this.key = new SecretKeySpec(key, "AES");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new IntegerWrapper(TYPE, value).toBlock();
    }
    
    
    /**
     * Returns the value of this symmetric key.
     * 
     * @return the value of this symmetric key.
     */
    @Pure
    public @Nonnull BigInteger getValue() {
        return value;
    }
    
    
    /**
     * Encrypts the indicated section in the given byte array with this symmetric key.
     * 
     * @param bytes the byte array of which a section is to be encrypted.
     * @param offset the offset of the section in the given byte array.
     * @param length the length of the section in the given byte array.
     * 
     * @return the encryption of the indicated section in the given byte array.
     * 
     * @require offset >= 0 : "The offset is not negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
     * 
     * @ensure return.length > 0 : "The returned byte array is not empty.";
     */
    @Pure
    public @Capturable @Nonnull byte[] encrypt(@Nonnull byte[] bytes, int offset, int length) {
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
        
        try {
            final @Nonnull Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes, offset, length);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            throw new ShouldNeverHappenError("Could not encrypt the given bytes.", exception);
        }
    }
    
    /**
     * Decrypts the indicated section in the given byte array with this symmetric key.
     * 
     * @param bytes the byte array of which a section is to be decrypted.
     * @param offset the offset of the section in the given byte array.
     * @param length the length of the section in the given byte array.
     * 
     * @return the decryption of the indicated section in the given byte array.
     * 
     * @require offset >= 0 : "The offset is not negative.";
     * @require length > 0 : "The length is positive.";
     * @require offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
     * 
     * @ensure return.length > 0 : "The returned byte array is not empty.";
     */
    @Pure
    public @Capturable @Nonnull byte[] decrypt(@Nonnull byte[] bytes, int offset, int length) throws InvalidEncodingException {
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
        
        try {
            final @Nonnull Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(bytes, offset, length);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            throw new InvalidEncodingException("Could not decrypt the given bytes.", exception);
        }
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof SymmetricKey)) return false;
        @Nonnull SymmetricKey other = (SymmetricKey) object;
        return this.value.equals(other.value);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
}
