package net.digitalid.core.cryptography.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.cryptography.InitializationVector;
import net.digitalid.utility.cryptography.InitializationVectorBuilder;
import net.digitalid.utility.cryptography.SymmetricKey;
import net.digitalid.utility.cryptography.SymmetricKeyBuilder;
import net.digitalid.utility.exceptions.UnexpectedFailureException;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;

import org.junit.Assert;
import org.junit.Test;

import static net.digitalid.utility.cryptography.SymmetricKey.*;

/**
 *
 */
public class EncryptionTest {
    
    public static final @Nonnull String MODE = "AES/CBC/PKCS5Padding";
    
    @Pure
    protected @Nonnull Key deriveKey(@Nonnull BigInteger value) {
        final @Nonnull byte[] bytes = value.toByteArray();
        final @Nonnull byte[] key = new byte[LENGTH];
        System.arraycopy(bytes, Math.max(bytes.length - LENGTH, 0), key, Math.max(LENGTH - bytes.length, 0), Math.min(LENGTH, bytes.length));
        return new SecretKeySpec(key, "AES");
    }

    @Pure
    public @Nonnull Cipher getEncryptCipher(@Nonnull SymmetricKey key, @Nonnull InitializationVector initializationVector) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        final @Nonnull Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(key.getValue()), initializationVector);
        return cipher;
    }

    @Pure
    public @Nonnull Cipher getDecryptCipher(@Nonnull SymmetricKey key, @Nonnull InitializationVector initializationVector) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        final @Nonnull Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(key.getValue()), initializationVector);
        return cipher;
    }
    
    @Pure
    private @Nonnull byte[] getAndInitializeRandomByteArray(int length) {
        @Nonnull Random random = new Random();
        @Nonnull byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
    
    @Pure
    private @Nonnull byte[] getAndInitializeByteArray(int length, byte content) {
        @Nonnull byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = content;
        }
        return bytes;
    }

    @Test
    public void shouldEncryptAndDecrypt() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[128];
        for (int i = 0; i < 128; i++) {
            bytes[i] = 1;
        }
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();

        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

        cipherOutputStream.write(bytes);
        cipherOutputStream.close();

        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();

        Assert.assertFalse(Arrays.equals(encryptedBytes, bytes));

        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));

        final @Nonnull byte[] decryptedBytes = new byte[128];
        cipherInputStream.read(decryptedBytes);

        Assert.assertTrue(Arrays.toString(bytes) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(bytes, decryptedBytes));
    }

    @Test
    public void shouldEncryptAndDecryptWithPadding() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Random random = new Random();
        byte[] bytes = new byte[29];
        random.nextBytes(bytes);

        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();

        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

        cipherOutputStream.write(bytes);
        cipherOutputStream.close();

        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();

        Assert.assertFalse(Arrays.equals(encryptedBytes, bytes));

        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));

        final int size = 29;
        final @Nonnull byte[] decryptedBytes = new byte[size];
        Assert.assertEquals(size, cipherInputStream.read(decryptedBytes));
        Assert.assertTrue(Arrays.toString(bytes) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(bytes, decryptedBytes));
    }

    @Test
    public void shouldEncryptAndDecryptMixedContent() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preEncrypted = new byte[128];
        for (int i = 0; i < 128; i++) {
            preEncrypted[i] = 2;
        }
        outputStream.write(preEncrypted);

        byte[] toEncrypt = new byte[128];
        for (int i = 0; i < 128; i++) {
            toEncrypt[i] = 1;
        }
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();

        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

        cipherOutputStream.write(toEncrypt);
        cipherOutputStream.close();

        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();

        Assert.assertFalse(Arrays.equals(encryptedBytes, toEncrypt));

        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);

        byte[] preDecrypted = new byte[128];
        inputStream.read(preDecrypted);
        Assert.assertTrue("Expected unencrypted bytes first", Arrays.equals(preEncrypted, preDecrypted));

        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));

        final @Nonnull byte[] decryptedBytes = new byte[128];
        cipherInputStream.read(decryptedBytes);

        Assert.assertTrue(Arrays.toString(toEncrypt) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(toEncrypt, decryptedBytes));
    }

    @Test
    public void shouldEncryptAndDecryptMixedContentWithPadding() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preEncrypted = new byte[13];
        for (int i = 0; i < 13; i++) {
            preEncrypted[i] = 2;
        }
        outputStream.write(preEncrypted);

        byte[] toEncrypt = new byte[29];
        for (int i = 0; i < 29; i++) {
            toEncrypt[i] = 1;
        }
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();

        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);

        cipherOutputStream.write(toEncrypt);
        cipherOutputStream.flush();
        cipherOutputStream.close();

        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();

        Assert.assertFalse(Arrays.equals(encryptedBytes, toEncrypt));

        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);

        byte[] preDecrypted = new byte[13];
        inputStream.read(preDecrypted);
        Assert.assertTrue("Expected unencrypted bytes first", Arrays.equals(preEncrypted, preDecrypted));

        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));

        int size = 29;
        final @Nonnull byte[] decryptedBytes = new byte[size];
        Assert.assertEquals(size, cipherInputStream.read(decryptedBytes));
        cipherInputStream.close();

        Assert.assertTrue(Arrays.toString(toEncrypt) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(toEncrypt, decryptedBytes));
    }
    
    @Test
    public void shouldEncryptAndDecryptMoreMixedContent() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preEncrypted = getAndInitializeByteArray(13, (byte) 2);
        outputStream.write(preEncrypted);
    
        int size = 32;
        byte[] toEncrypt = getAndInitializeRandomByteArray(size);
        byte[] postEncrypt = getAndInitializeByteArray(13, (byte) 3);
        
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull NonClosingOutputStream nonClosingOutputStream = new NonClosingOutputStream(outputStream);
        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(nonClosingOutputStream, cipher);
        
        cipherOutputStream.write(toEncrypt);
        cipherOutputStream.close();
        
        outputStream.write(postEncrypt);
        nonClosingOutputStream.actualClose();
        
        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();
        
        Assert.assertFalse(Arrays.equals(encryptedBytes, toEncrypt));
        
        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
        
        byte[] preDecrypted = new byte[13];
        inputStream.read(preDecrypted);
        Assert.assertTrue("Expected unencrypted bytes first", Arrays.equals(preEncrypted, preDecrypted));
        
        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));
        
        final @Nonnull byte[] decryptedBytes = new byte[size];
        cipherInputStream.read(decryptedBytes);
       
        Assert.assertTrue(Arrays.toString(toEncrypt) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(toEncrypt, decryptedBytes));
    
        byte[] postDecrypted = new byte[13];
        int read = cipherInputStream.getPlainInputStream().read(postDecrypted);
        Assert.assertEquals(13, read);
        inputStream.close();
        Assert.assertTrue(Arrays.toString(postEncrypt) + " != " + Arrays.toString(postDecrypted), Arrays.equals(postEncrypt, postDecrypted));
    }
    
    @Test
    public void shouldEncryptAndDecryptLongerMixedContent() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preEncrypted = getAndInitializeByteArray(13, (byte) 2);
        outputStream.write(preEncrypted);
    
        int size = 1008;
        byte[] toEncrypt = getAndInitializeRandomByteArray(size);
        byte[] postEncrypt = getAndInitializeByteArray(13, (byte) 3);
        
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull NonClosingOutputStream nonClosingOutputStream = new NonClosingOutputStream(outputStream);
        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(nonClosingOutputStream, cipher);
        
        cipherOutputStream.write(toEncrypt);
        cipherOutputStream.close();
        
        outputStream.write(postEncrypt);
        nonClosingOutputStream.actualClose();
        
        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();
        
        Assert.assertFalse(Arrays.equals(encryptedBytes, toEncrypt));
        
        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
        
        byte[] preDecrypted = new byte[13];
        inputStream.read(preDecrypted);
        Assert.assertTrue("Expected unencrypted bytes first", Arrays.equals(preEncrypted, preDecrypted));
        
        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));
        
        final @Nonnull byte[] decryptedBytes = new byte[size];
        cipherInputStream.read(decryptedBytes);
       
        Assert.assertTrue(Arrays.toString(toEncrypt) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(toEncrypt, decryptedBytes));
    
        byte[] postDecrypted = new byte[13];
        int read = cipherInputStream.getPlainInputStream().read(postDecrypted);
        Assert.assertEquals(13, read);
        inputStream.close();
        Assert.assertTrue(Arrays.toString(postEncrypt) + " != " + Arrays.toString(postDecrypted), Arrays.equals(postEncrypt, postDecrypted));
    }
    
    @Test
    public void shouldEncryptAndDecryptMixedContentRandomLengthStressTest() throws Exception {
        Random random = new Random();
        int size = -1;
        try {
            for (int i = 0; i < 10000; i++) {
                final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] preEncrypted = getAndInitializeByteArray(13, (byte) 2);
                outputStream.write(preEncrypted);
        
                size = random.nextInt(8192);
    
                byte[] toEncrypt = getAndInitializeRandomByteArray(size);
                byte[] postEncrypt = getAndInitializeByteArray(13, (byte) 3);
    
                final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
                final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
    
                final @Nonnull NonClosingOutputStream nonClosingOutputStream = new NonClosingOutputStream(outputStream);
                final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
                final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(nonClosingOutputStream, cipher);
    
                cipherOutputStream.write(toEncrypt);
                cipherOutputStream.close();
    
                outputStream.write(postEncrypt);
                nonClosingOutputStream.actualClose();
    
                final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();
    
                Assert.assertFalse(Arrays.equals(encryptedBytes, toEncrypt));
    
                final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
    
                byte[] preDecrypted = new byte[13];
                inputStream.read(preDecrypted);
                Assert.assertTrue("Expected unencrypted bytes first", Arrays.equals(preEncrypted, preDecrypted));
    
                final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));
    
                final @Nonnull byte[] decryptedBytes = new byte[size];
                cipherInputStream.read(decryptedBytes);
    
                Assert.assertTrue(Arrays.toString(toEncrypt) + " != " + Arrays.toString(decryptedBytes), Arrays.equals(toEncrypt, decryptedBytes));
    
                byte[] postDecrypted = new byte[13];
                int read = cipherInputStream.getPlainInputStream().read(postDecrypted);
                Assert.assertEquals(13, read);
                inputStream.close();
                Assert.assertTrue(Arrays.toString(postEncrypt) + " != " + Arrays.toString(postDecrypted), Arrays.equals(postEncrypt, postDecrypted));
            }
        } catch (Exception e) {
            UnexpectedFailureException.with("Failed encryption/decyption with byte array of size '" + size + "'", e);
        }
    }
    
    @Test
    public void shouldEncryptAndDecryptWhenWritingMultipleTimes() throws Exception {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] preEncrypted = getAndInitializeByteArray(13, (byte) 2);
        outputStream.write(preEncrypted);
    
        final int size1 = 8192;
        final int size2 = 500;
        final int size3 = 29;
            
        byte[] toEncrypt1 = getAndInitializeRandomByteArray(size1);
        byte[] toEncrypt2 = getAndInitializeRandomByteArray(size2);
        byte[] toEncrypt3 = getAndInitializeRandomByteArray(size3);
        byte[] postEncrypt = getAndInitializeByteArray(13, (byte) 3);

        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();

        final @Nonnull NonClosingOutputStream nonClosingOutputStream = new NonClosingOutputStream(outputStream);
        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(nonClosingOutputStream, cipher);

        cipherOutputStream.write(toEncrypt1);
        cipherOutputStream.write(toEncrypt2);
        cipherOutputStream.write(toEncrypt3);
        cipherOutputStream.close();

        outputStream.write(postEncrypt);
        nonClosingOutputStream.actualClose();

        final @Nonnull byte[] encryptedBytes = outputStream.toByteArray();

        Assert.assertFalse(Arrays.equals(encryptedBytes, toEncrypt1));

        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);

        byte[] preDecrypted = new byte[13];
        inputStream.read(preDecrypted);
        Assert.assertTrue("Expected unencrypted bytes first", Arrays.equals(preEncrypted, preDecrypted));

        final @Nonnull CustomCipherInputStream cipherInputStream = new CustomCipherInputStream(inputStream, getDecryptCipher(symmetricKey, initializationVector));

        final @Nonnull byte[] decryptedBytes1 = new byte[size1];
        cipherInputStream.read(decryptedBytes1);
        final @Nonnull byte[] decryptedBytes2 = new byte[size2];
        cipherInputStream.read(decryptedBytes2);
        final @Nonnull byte[] decryptedBytes3 = new byte[size3];
        cipherInputStream.read(decryptedBytes3);

        Assert.assertTrue(Arrays.toString(toEncrypt1) + " != " + Arrays.toString(decryptedBytes1), Arrays.equals(toEncrypt1, decryptedBytes1));
        Assert.assertTrue(Arrays.toString(toEncrypt2) + " != " + Arrays.toString(decryptedBytes2), Arrays.equals(toEncrypt2, decryptedBytes2));
        Assert.assertTrue(Arrays.toString(toEncrypt3) + " != " + Arrays.toString(decryptedBytes3), Arrays.equals(toEncrypt3, decryptedBytes3));

        byte[] postDecrypted = new byte[13];
        int read = cipherInputStream.getPlainInputStream().read(postDecrypted);
        Assert.assertEquals(13, read);
        inputStream.close();
        Assert.assertTrue(Arrays.toString(postEncrypt) + " != " + Arrays.toString(postDecrypted), Arrays.equals(postEncrypt, postDecrypted));
    }
    
    @Test
    public void shouldEncryptAndDecryptStringBytes() throws Exception {
        final @Nonnull String secret = "This is a secret";
        final @Nonnull byte[] secretBytes = secret.getBytes("UTF-16BE");
    
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
    
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final @Nonnull NonClosingOutputStream nonClosingOutputStream = new NonClosingOutputStream(outputStream);
        final @Nonnull Cipher cipher = getEncryptCipher(symmetricKey, initializationVector);
        final @Nonnull CipherOutputStream cipherOutputStream = new CipherOutputStream(nonClosingOutputStream, cipher);
        
        cipherOutputStream.write(secretBytes);
        cipherOutputStream.write(0);
        cipherOutputStream.write(0);
        cipherOutputStream.close();
    
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
        final @Nonnull CustomCipherInputStream customCipherInputStream = new CustomCipherInputStream(byteArrayInputStream, getDecryptCipher(symmetricKey, initializationVector));
        
        
        @MultipleOf(2) int length = 32;
        @Nonnull byte[] string = new byte[length];
        int i = 0;
        final byte[] character = new byte[2];
        while (customCipherInputStream.read(character) > 0 && (character[0] != 0 || character[1] != 0)) {
            string[i] = character[0];
            string[i + 1] = character[1];
            i += 2;
            if (i == length) {
                length = length * 2;
                string = Arrays.copyOf(string, length);
            }
            character[0] = character[1] = 0;
        }
        final @Nonnull String decryptedSecret = new String(string, 0, i, "UTF-16BE");
        Assert.assertEquals(secret, decryptedSecret);
    }
    
}
