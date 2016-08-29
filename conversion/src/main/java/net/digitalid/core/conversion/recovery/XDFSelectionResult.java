package net.digitalid.core.conversion.recovery;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.functional.failable.FailableProducer;
import net.digitalid.utility.logging.exceptions.io.StreamException;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.Size;

import net.digitalid.core.cryptography.compression.BufferedInflaterInputStream;
import net.digitalid.core.cryptography.encryption.CustomCipherInputStream;

/**
 *
 */
public class XDFSelectionResult implements SelectionResult<StreamException> {
    
    /**
     * The input stream from which we are reading.
     */
    private @Nonnull Stack<@Nonnull InputStream> inputStreamStack = new Stack<>();
    
    @Pure
    public @Nonnull InputStream getInputStream() {
        return inputStreamStack.peek();
    }
    
    XDFSelectionResult(@Nonnull InputStream inputStream) {
        inputStreamStack.add(inputStream);
    }
    
    @Pure
    public static @Nonnull XDFSelectionResult with(@Nonnull InputStream inputStream) {
        return new XDFSelectionResult(inputStream);
    }
    
    @Impure
    @Override
    public void getEmpty() {
        
    }
    
    @Impure
    @Override
    public boolean getBoolean() throws StreamException {
        try {
            return getInputStream().read() == 1;
        } catch (IOException e) {
            throw StreamException.with("Failed to read boolean value from input stream", e);
        }
    }
    
    @Impure
    @Override
    public byte getInteger08() {
        return 0;
    }
    
    @Impure
    @Override
    public short getInteger16() {
        return 0;
    }
    
    @Impure
    @Override
    public int getInteger32() {
        return 0;
    }
    
    @Impure
    @Override
    public long getInteger64() throws StreamException {
        try {
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (getInputStream().read() & 0xff);
            }
            return value;
        } catch (IOException e) {
            throw StreamException.with("Failed to read long value from input stream", e);
        }
    }
    
    @Impure
    @Override
    public @Nullable BigInteger getInteger() throws StreamException {
        try {
            final byte sizeOfByteArray = (byte) getInputStream().read();
            byte[] bigIntegerByteArray = new byte[sizeOfByteArray];
            getInputStream().read(bigIntegerByteArray);
            return new BigInteger(bigIntegerByteArray);
        } catch (IOException e) {
            throw StreamException.with("Failed to read BigInteger value from input stream", e);
        }
    }
    
    @Impure
    @Override
    public float getDecimal32() {
        return 0;
    }
    
    @Impure
    @Override
    public double getDecimal64() {
        return 0;
    }
    
    @Impure
    @Override
    public char getString01() {
        return 0;
    }
    
    @Impure
    @Override
    public @Nullable @MaxSize(64) String getString64() {
        return null;
    }
    
    @Impure
    @Override
    public @Nullable String getString() throws StreamException {
        try {
            @MultipleOf(2) int length = 32;
            @Nonnull byte[] string = new byte[length];
            int i = 0;
            final byte[] character = new byte[2];
            while (getInputStream().read(character) > 0 && (character[0] != 0 || character[1] != 0)) {
                string[i] = character[0];
                string[i + 1] = character[1];
                i += 2;
                if (i == length) {
                    length = length * 2;
                    string = Arrays.copyOf(string, length);
                }
                character[0] = character[1] = 0;
            }
            return new String(string, 0, i, "UTF-16BE");
        } catch (IOException e) {
            throw StreamException.with("Failed to read string value from input stream", e);
        }
    }
    
    @Impure
    @Override
    public @Nullable @Size(16) byte[] getBinary128() {
        return new byte[0];
    }
    
    @Impure
    @Override
    public @Nullable @Size(32) byte[] getBinary256() {
        return new byte[0];
    }
    
    @Impure
    @Override
    public @Nullable byte[] getBinary() throws StreamException {
        try {
            int byteArraySize = getInputStream().read();
            byte[] bytes = new byte[byteArraySize];
            int read = getInputStream().read(bytes);
            Require.that(read == byteArraySize).orThrow("The input stream ended unexpectedly");
            
            return bytes;
        } catch (IOException e) {
            throw StreamException.with("Failed to read binary array from input stream", e);
        }
    }
    
    @Impure
    @Override
    public <T> List<T> getList(@Nonnull FailableProducer<T, StreamException> function) throws StreamException {
        try {
            int listSize = getInputStream().read();
            final @Nonnull List<@Nullable T> list = FreezableArrayList.withInitialCapacity(listSize);
            for (int i = 0; i < listSize; i++) {
                final @Nullable T element = function.produce();
                list.add(element);
            }
            return list;
        } catch (IOException e) {
            throw StreamException.with("Failed to read list values from input stream", e);
        }
    }
    
    @Impure
    @Override
    public <T> T[] getArray(@Nonnull FailableProducer<T, StreamException> function) {
        return null;
    }
    
    @Impure
    @Override
    public <T> Set<T> getSet(@Nonnull FailableProducer<T, StreamException> function) {
        return null;
    }
    
    @Impure
    @Override
    public <K, V> Map<K, V> getMap(@Nonnull FailableProducer<K, StreamException> keyFunction, @Nonnull FailableProducer<V, StreamException> valueFunction) {
        return null;
    }
    
    @Impure
    @Override
    public void setDecryptionCipher(@Nonnull Cipher cipher) {
        inputStreamStack.add(CustomCipherInputStream.with(getInputStream(), cipher));
    }
    
    @Impure
    @Override
    public void popDecryptionCipher() {
        Require.that(getInputStream() instanceof CustomCipherInputStream).orThrow("Cipher input stream not found.");
    
        inputStreamStack.pop();
    }
    
    @Impure
    @Override
    public void setDecompression(@Nonnull Inflater inflater) {
        inputStreamStack.add(new BufferedInflaterInputStream(getInputStream()));
    }
    
    @Impure
    @Override
    public void popDecompression() throws StreamException {
        Require.that(getInputStream() instanceof BufferedInflaterInputStream).orThrow("Inflater input stream not found.");
    
        final @Nonnull BufferedInflaterInputStream inflaterInputStream = (BufferedInflaterInputStream) inputStreamStack.pop();
        inputStreamStack.set(inputStreamStack.size() - 1, inflaterInputStream.finish());
    }
    
    @Impure
    @Override
    public void setSignatureDigest(@Nonnull MessageDigest digest) {
        
    }
    
    @Impure
    @Override
    public @Nonnull DigestInputStream popSignatureDigest() {
        return null;
    }
    
}
