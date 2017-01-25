package net.digitalid.core.symmetrickey;

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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.errors.SupportErrorBuilder;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.math.Positive;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.parameters.Parameters;

/**
 * Symmetric keys are used to encrypt and decrypt byte arrays with the Advanced Encryption Standard (AES).
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class SymmetricKey extends RootClass {
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target.
     */
    public static final @Nonnull Configuration<String> configuration = Configuration.with("");
    
    /* -------------------------------------------------- Circumvent Cryptographic Restrictions -------------------------------------------------- */
    
    /**
     * Initializes the maximum allowed key length of AES by circumventing the Java Runtime Environment restrictions.
     */
    @Pure
    @Initialize(target = SymmetricKey.class)
    @TODO(task = "Consider using Bouncy Castle as a JRE-independent solution to the following hack.", date = "2016-04-19", author = Author.KASPAR_ETTER, priority = Priority.LOW)
    public static void initializeKeyLength() {
        try {
            final int length = Cipher.getMaxAllowedKeyLength("AES");
            if (length < Parameters.ENCRYPTION_KEY.get()) {
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
                        throw SupportErrorBuilder.withMessage("Your system allows only a maximal key length of " + length + " bits for symmetric encryption but a length of " + Parameters.ENCRYPTION_KEY.get() + " bits is required for security reasons."
                                + "Please install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files from http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html for Java 8."
                                + "(All you have to do is to download the files and replace with them 'local_policy.jar' and 'US_export_policy.jar' in '" + System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "'.)").withCause(exception).build();
                    }
                }
            }
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw SupportErrorBuilder.withMessage("Your system does not support the Advanced Encryption Standard (AES). Unfortunately, you are not able to use Digital ID for now.").withCause(exception).build();
        }
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns a random value with the right length.
     */
    @Pure
    public static @Nonnull BigInteger getRandomValue() {
        return new BigInteger(Parameters.ENCRYPTION_KEY.get(), new SecureRandom());
    }
    
    /**
     * Returns the value of this symmetric key.
     */
    @Pure
    @Default(name = "RandomValue", value = "SymmetricKey.getRandomValue()")
    public abstract @Nonnull BigInteger getValue();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Stores the length of symmetric keys in bytes.
     */
    public static final int LENGTH = Parameters.ENCRYPTION_KEY.get() / 8;
    
    /**
     * Derives the key from the given value.
     */
    @Pure
    protected static @Nonnull Key deriveKey(@Nonnull BigInteger value) {
        final @Nonnull byte[] bytes = value.toByteArray();
        final @Nonnull byte[] key = new byte[LENGTH];
        System.arraycopy(bytes, Math.max(bytes.length - LENGTH, 0), key, Math.max(LENGTH - bytes.length, 0), Math.min(LENGTH, bytes.length));
        return new SecretKeySpec(key, "AES");
    }
    
    /**
     * Returns the key of this symmetric key.
     */
    @Pure
    @Derive("SymmetricKey.deriveKey(value)")
    protected abstract @Nonnull Key getKey();
    
    /* -------------------------------------------------- Encryption and Decryption -------------------------------------------------- */
    
    /**
     * Stores the mode of the encryption cipher.
     */
    public static final @Nonnull String MODE = "AES/CBC/PKCS5Padding";
    
    /**
     * Initializes and returns the cipher of this symmetric key.
     * 
     * @param cipherMode the cipher mode like Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE.
     */
    @Pure
    public @Nonnull Cipher getCipher(@Nonnull InitializationVector initializationVector, int cipherMode) {
        try {
            final @Nonnull Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(cipherMode, getKey(), initializationVector);
            return cipher;
        } catch (@Nonnull NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException  exception) {
            throw SupportErrorBuilder.withMessage("Could not initialize the cipher.").withCause(exception).build();
        }
    }
    /**
     * Encrypts the indicated section in the given byte array with this symmetric key and the given initialization vector.
     * 
     * @require offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
     */
    @Pure
    public @Capturable @Nonnull @NonEmpty byte[] encrypt(@Nonnull InitializationVector initializationVector, @NonCaptured @Unmodified @Nonnull @NonEmpty byte[] bytes, @NonNegative int offset, @Positive int length) {
        Require.that(offset + length <= bytes.length).orThrow("The indicated section may not exceed the given byte array.");
        
        try {
            return getCipher(initializationVector, Cipher.ENCRYPT_MODE).doFinal(bytes, offset, length);
        } catch (@Nonnull IllegalBlockSizeException | BadPaddingException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /**
     * Decrypts the indicated section in the given byte array with this symmetric key and the given initialization vector.
     * 
     * @require offset + length <= bytes.length : "The indicated section may not exceed the given byte array.";
     */
    @Pure
    public @Capturable @Nonnull @NonEmpty byte[] decrypt(@Nonnull InitializationVector initializationVector, @NonCaptured @Unmodified @Nonnull @NonEmpty byte[] bytes, @NonNegative int offset, @Positive int length) {
        Require.that(offset + length <= bytes.length).orThrow("The indicated section may not exceed the given byte array.");
        
        try {
            return getCipher(initializationVector, Cipher.DECRYPT_MODE).doFinal(bytes, offset, length);
        } catch (@Nonnull IllegalBlockSizeException | BadPaddingException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
}
