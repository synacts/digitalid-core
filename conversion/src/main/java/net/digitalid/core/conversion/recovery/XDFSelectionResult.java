package net.digitalid.core.conversion.recovery;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.functional.failable.FailableProducer;
import net.digitalid.utility.logging.exceptions.io.StreamException;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.Size;

import net.digitalid.core.compression.BufferedInflaterInputStream;
import net.digitalid.core.conversion.utility.StackHandler;
import net.digitalid.core.encryption.CustomCipherInputStream;

/**
 *
 */
public class XDFSelectionResult implements SelectionResult<StreamException> {
    
    /* -------------------------------------------------- Input Stream Stack Operations -------------------------------------------------- */
    
    /**
     * The stack handler keeps track of the state of the input stream. It holds a stack of any input streams, but guarantees
     * that the top entry is always a data input stream. When an input stream is added to the stack, it immediately adds a data input stream that wraps the previously added input stream.
     * When an input stream is popped, the data input stream is popped too.
     */
    private static class InputStreamStackHandler extends StackHandler<@Nonnull InputStream, @Nonnull DataInputStream> {
        
        @Pure
        @Override
        protected @Nonnull DataInputStream wrapEntry(@Nonnull InputStream stackEntry) {
            return new DataInputStream(stackEntry);
        }
        
        @Pure
        @Override
        protected @Nonnull Class<@Nonnull DataInputStream> getWrapperType() {
            return DataInputStream.class;
        }
        
    }
    
    private final @Nonnull InputStreamStackHandler inputStreamStackHandler = new InputStreamStackHandler();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    XDFSelectionResult(@Nonnull InputStream inputStream) {
        inputStreamStackHandler.addAndWrapStackEntry(inputStream);
    }
    
    @Pure
    public static @Nonnull XDFSelectionResult with(@Nonnull InputStream inputStream) {
        return new XDFSelectionResult(inputStream);
    }
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
    @Impure
    @Override
    public void getEmpty() {
        
    }
    
    @Impure
    @Override
    public boolean getBoolean() throws StreamException {
        try {
            return inputStreamStackHandler.peek().readBoolean();
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
            return inputStreamStackHandler.peek().readLong();
        } catch (IOException e) {
            throw StreamException.with("Failed to read long value from input stream", e);
        }
    }
    
    @Impure
    @Override
    public @Nullable BigInteger getInteger() throws StreamException {
        try {
            final byte sizeOfByteArray = (byte) inputStreamStackHandler.peek().read();
            byte[] bigIntegerByteArray = new byte[sizeOfByteArray];
            inputStreamStackHandler.peek().read(bigIntegerByteArray);
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
            return inputStreamStackHandler.peek().readUTF();
        } catch (IOException e) {
            throw StreamException.with("Failed to read UTF-8 string value from input stream", e);
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
            int byteArraySize = inputStreamStackHandler.peek().read();
            byte[] bytes = new byte[byteArraySize];
            int read = inputStreamStackHandler.peek().read(bytes);
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
            int listSize = inputStreamStackHandler.peek().readInt();
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
        inputStreamStackHandler.addAndWrapStackEntry(CustomCipherInputStream.with(inputStreamStackHandler.peek(), cipher));
    }
    
    @Impure
    @Override
    public void popDecryptionCipher() {
        inputStreamStackHandler.popWrappedStackEntry(CustomCipherInputStream.class);
    }
    
    @Impure
    @Override
    public void setDecompression(@Nonnull Inflater inflater) {
        inputStreamStackHandler.addAndWrapStackEntry(new BufferedInflaterInputStream(inputStreamStackHandler.peek()));
    }
    
    @Impure
    @Override
    public void popDecompression() throws StreamException {
        final @Nonnull BufferedInflaterInputStream bufferedInflaterInputStream = inputStreamStackHandler.popWrappedStackEntry(BufferedInflaterInputStream.class);
        bufferedInflaterInputStream.finish();
    }
    
    @Impure
    @Override
    public void setSignatureDigest(@Nonnull MessageDigest digest) {
        inputStreamStackHandler.addAndWrapStackEntry(new DigestInputStream(inputStreamStackHandler.peek(), digest));
    }
    
    @Impure
    @Override
    public @Nonnull DigestInputStream popSignatureDigest() {
        return inputStreamStackHandler.popWrappedStackEntry(DigestInputStream.class);
    }
    
}
