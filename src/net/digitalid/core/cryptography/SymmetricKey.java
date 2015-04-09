package net.digitalid.core.cryptography;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.SecureRandom;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.errors.InitializationError;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.IntegerWrapper;

/**
 * Symmetric keys are used to encrypt and decrypt byte arrays with the Advanced Encryption Standard (AES).
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class SymmetricKey implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code symmetric.key@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("symmetric.key@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    
    static {
        try {
            final int length = Cipher.getMaxAllowedKeyLength("AES");
            if (length < Parameters.ENCRYPTION_KEY) {
                if (System.getProperty("java.runtime.name").equals("Java(TM) SE Runtime Environment")) {
                    /*
                     * Do the following, but with reflection to bypass access checks (taken from http://stackoverflow.com/a/22492582):
                     *
                     * JceSecurity.isRestricted = false;
                     * JceSecurity.defaultPolicy.perms.clear();
                     * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
                     */
                    try {
                        final @Nonnull Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
                        final @Nonnull Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
                        final @Nonnull Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");
                        
                        final @Nonnull Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
                        isRestrictedField.setAccessible(true);
                        isRestrictedField.set(null, false);
                        
                        final @Nonnull Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
                        defaultPolicyField.setAccessible(true);
                        final @Nonnull PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);
                        
                        final @Nonnull Field permsField = cryptoPermissions.getDeclaredField("perms");
                        permsField.setAccessible(true);
                        ((Map<?, ?>) permsField.get(defaultPolicy)).clear();
                        
                        final @Nonnull Field instanceField = cryptoAllPermission.getDeclaredField("INSTANCE");
                        instanceField.setAccessible(true);
                        defaultPolicy.add((Permission) instanceField.get(null));
                    } catch (@Nonnull ClassNotFoundException | NoSuchFieldException | IllegalArgumentException | SecurityException | IllegalAccessException exception) {
                        throw new InitializationError("Your system allows only a maximal key length of " + length + " bits for symmetric encryption but a length of " + Parameters.ENCRYPTION_KEY + " bits is required for security reasons."
                                + "Please install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files from http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html for Java 7."
                                + "(All you have to do is to download the files and replace with them 'local_policy.jar' and 'US_export_policy.jar' in '" + System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "'.)", exception);
                    }
                }
            }
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw new InitializationError("Your system does not support the Advanced Encryption Standard (AES). Unfortunately, you are not able to use Digital ID for now.", exception);
        }
    }
    
    
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
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public SymmetricKey(@Nonnull Block block) throws InvalidEncodingException {
        this(new IntegerWrapper(block).getValue());
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
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
     * @param initializationVector the initialization vector for the encryption.
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
    public @Capturable @Nonnull byte[] encrypt(@Nonnull InitializationVector initializationVector, @Nonnull byte[] bytes, int offset, int length) {
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
        
        try {
            final @Nonnull Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.ENCRYPT_MODE, key, initializationVector);
            return cipher.doFinal(bytes, offset, length);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException exception) {
            throw new ShouldNeverHappenError("Could not encrypt the given bytes.", exception);
        }
    }
    
    /**
     * Decrypts the indicated section in the given byte array with this symmetric key.
     * 
     * @param initializationVector the initialization vector for the decryption.
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
    public @Capturable @Nonnull byte[] decrypt(@Nonnull InitializationVector initializationVector, @Nonnull byte[] bytes, int offset, int length) throws InvalidEncodingException {
        assert offset >= 0 : "The offset is not negative.";
        assert length > 0 : "The length is positive.";
        assert offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
        
        try {
            final @Nonnull Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.DECRYPT_MODE, key, initializationVector);
            return cipher.doFinal(bytes, offset, length);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException exception) {
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
