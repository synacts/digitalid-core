package net.digitalid.core.conversion.streams.input;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.math.Positive;

/**
 * The custom cipher input stream can read encrypted data, decrypt it using a given cipher and, unlike 
 * Java's {@link CipherInputStream cipher input stream}, return an input stream which allows further reading 
 * plain text from.
 */
@GenerateSubclass // TODO: Why is a subclass generated if it's not instantiated?
public class CustomCipherInputStream extends FilterInputStream {
    
    /**
     * Temporary buffer that stores the data that is read from the input stream.
     */
    private byte[] buffer = new byte[512];
    
    /**
     * Marks where reading of the {@link #buffer} should end.
     */
    private @NonNegative int finishEncryptedBuffer = 0;
    
    /**
     * The cipher with which the input stream is decrypted.
     */
    private final @Nonnull Cipher cipher;
    
    /**
     * The buffer of decrypted bytes.
     */
    private byte[] decrypted;
    
    /**
     * Marks the end of the decrypted buffer.
     */
    private int finishDecryptedBuffer;
    
    /**
     * Marks the start of the decrypted buffer.
     */
    private int startDecryptedBuffer;
    
    private int readDecryptedAll;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new custom cipher input stream with the given input stream and cipher.
     */
    CustomCipherInputStream(@Nonnull InputStream inputStream, @Nonnull Cipher cipher) {
        super(inputStream);
        Require.that(cipher.getAlgorithm().equals("AES/CBC/PKCS5Padding")).orThrow("The cipher algorithm $ is currently unsupported.", cipher.getAlgorithm());
        
        this.cipher = cipher;
    }
    
    /**
     * Returns a custom cipher input stream with the given input stream and cipher.
     */
    @Pure
    public static @Nonnull CustomCipherInputStream with(@Nonnull InputStream inputStream, @Nonnull Cipher cipher) {
        return new CustomCipherInputStream(inputStream, cipher);
    }
    
    /* -------------------------------------------------- Read -------------------------------------------------- */
    
    @Impure
    private int updatePointersToDecryptedBuffer() {
        this.finishDecryptedBuffer = this.decrypted.length;
        this.startDecryptedBuffer = 0;
        return this.finishDecryptedBuffer - this.startDecryptedBuffer;
    }
    
    @Impure
    @Override
    public int read() throws IOException {
        byte[] singleByte = new byte[1];
        read(singleByte);
        return singleByte[0];
    }
    
    /**
     * Reads data from the input stream into a buffer, decrypts the buffer and returns its content.
     */
    @Impure
    @Override
    public int read(@Nonnull byte[] bytes, @NonNegative int offset, final @Positive int length) throws IOException {
        int readRound = 0;
        int maxRead = length / buffer.length + 1;
        int desiredRoundLengthDecryptedBuffer;
        int readDecrypted = 0;
        int maxDecryptedBufferLength = ((buffer.length / cipher.getBlockSize()) - 1) * cipher.getBlockSize();
        
        do {
            // the maximum of bytes that can be read from the decrypted buffer, if we set the encrypted buffer to buffer.length.
            desiredRoundLengthDecryptedBuffer = Math.min(length - readDecrypted, maxDecryptedBufferLength);
            int readEncrypted = 0;
            int remainingFromDecryptedBuffer = this.finishDecryptedBuffer - this.startDecryptedBuffer;
            if (remainingFromDecryptedBuffer == 0) {
                if (readRound < maxRead) {
                    readEncrypted = super.in.read(buffer);
                    this.finishEncryptedBuffer = readEncrypted;
                    readRound++;
                }
                if (readEncrypted == 0 || readEncrypted == -1) {
                    try {
                        this.decrypted = this.cipher.doFinal();
                    } catch (BadPaddingException | IllegalBlockSizeException e) {
                        throw new IOException(e);
                    }
                } else {
                    if (readEncrypted % cipher.getBlockSize() != 0) {
                        readEncrypted = ((readEncrypted / cipher.getBlockSize()) + 1) * cipher.getBlockSize();
                    }
                    this.decrypted = this.cipher.update(buffer, 0, readEncrypted);
                }
                remainingFromDecryptedBuffer = updatePointersToDecryptedBuffer();
            } if (desiredRoundLengthDecryptedBuffer > remainingFromDecryptedBuffer) {
                // store the remaining decrypted buffer in the bytes.
//                Require.that(round == 0).orThrow("Expected to fetch data from the already decrypted buffer only in the first round (desired round length of decrypted buffer: $, remaining bytes in decrypted buffer: $).", desiredRoundLengthDecryptedBuffer, remainingFromDecryptedBuffer);
        
                System.arraycopy(decrypted, startDecryptedBuffer, bytes, readDecrypted, remainingFromDecryptedBuffer);
                readDecrypted += remainingFromDecryptedBuffer;
                this.startDecryptedBuffer = 0;
                this.finishDecryptedBuffer = 0;
                remainingFromDecryptedBuffer = 0;
            }
            if (remainingFromDecryptedBuffer > 0) {
                System.arraycopy(decrypted, startDecryptedBuffer, bytes, readDecrypted, desiredRoundLengthDecryptedBuffer);
                readDecrypted += desiredRoundLengthDecryptedBuffer;
                this.startDecryptedBuffer = this.startDecryptedBuffer + desiredRoundLengthDecryptedBuffer;
            }
        } while (readDecrypted < length);
        this.readDecryptedAll += readDecrypted;
        return readDecrypted;
    }
    
    /* -------------------------------------------------- Available -------------------------------------------------- */
    
    @Pure
    @Override
    public int available() throws IOException {
        return super.available();
    }
    
    /* -------------------------------------------------- Plain Input Stream -------------------------------------------------- */
    
    /**
     * Wraps a byte array input stream around the remaining buffer content, appends the original input stream and returns both as sequence input stream.
     */
    @Pure
    public @Nonnull InputStream getPlainInputStream() throws IOException {
        if (finishEncryptedBuffer <= 0) {
            return super.in;
        } else {
            final int firstUnprocessedByteInBuffer = (((readDecryptedAll / cipher.getBlockSize()) + 1) * cipher.getBlockSize()) % buffer.length;
            if (firstUnprocessedByteInBuffer != 0) {
                return new SequenceInputStream(new ByteArrayInputStream(buffer, firstUnprocessedByteInBuffer, finishEncryptedBuffer - firstUnprocessedByteInBuffer), super.in);
            } else {
                return super.in;
            }
        }
    }
    
}
