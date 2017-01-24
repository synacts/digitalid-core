package net.digitalid.core.conversion;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.interfaces.EncoderImplementation;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.functional.failable.FailableUnaryFunction;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.Size;

import net.digitalid.core.conversion.exceptions.StreamException;
import net.digitalid.core.conversion.streams.NonClosingOutputStream;

/**
 *
 */
public class XDFEncoder extends EncoderImplementation<StreamException> {
    
    /**
     * The stack handler keeps track of the state of the output stream. It holds a stack of any output streams, but guarantees
     * that the top entry is always a data output stream. When an output stream is added to the stack, it immediately adds a data output stream that wraps the previously added data stream.
     * When an output stream is popped, the data output stream is popped too.
     */
    private static class OutputStreamStackHandler extends StackHandler<@Nonnull OutputStream, @Nonnull DataOutputStream> {
        
        @Pure
        @Override
        protected @Nonnull DataOutputStream wrapEntry(@Nonnull OutputStream stackEntry) {
            return new DataOutputStream(stackEntry);
        }
        
        @Pure
        @Override
        protected @Nonnull Class<@Nonnull DataOutputStream> getWrapperType() {
            return DataOutputStream.class;
        }
        
    }
    
    private final @Nonnull OutputStreamStackHandler outputStreamStack;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new XDF value collector instance.
     */
    private XDFEncoder(@Nonnull OutputStream outputStream) {
        this.outputStreamStack = new OutputStreamStackHandler();
        outputStreamStack.addAndWrapStackEntry(outputStream);
    }
    
    @Pure
    public void finish() {
        Require.that(outputStreamStack.hasSize(2)).orThrow("Expected a stack size of 2 (the initial output stream and the wrapped output stream), but got $", outputStreamStack.size());
    }
    
    /**
     * Returns an XDF value collector for a given output stream.
     */
    @Pure
    public static @Nonnull XDFEncoder with(@Nonnull OutputStream outputStream) {
        return new XDFEncoder(outputStream);
    }
    
    /* -------------------------------------------------- Representation -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Representation getRepresentation() {
        return Representation.EXTERNAL;
    }
    
    /* -------------------------------------------------- Overridden Collector Methods -------------------------------------------------- */
       
    @Impure
    @Override
    public Integer encodeEmpty() {
        // TODO: unclear if this should be supported. What's the difference to null?
        return null;
    }
    
    @Impure
    @Override
    public Integer setBoolean(boolean value) throws StreamException {
        try {
            outputStreamStack.peek().writeBoolean(value);
        } catch (IOException e) {
            throw StreamException.with("Failed to write boolean value to output stream", e);
        }
        return 1;
    }
    
    @Impure
    @Override
    public Integer setInteger08(byte value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setInteger16(short value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setInteger32(int value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setInteger64(long value) throws StreamException {
        try {
            outputStreamStack.peek().writeLong(value);
        } catch (IOException e) {
            throw StreamException.with("Failed to write long value to output stream", e);
        }
        return 1;
    }
    
    @Impure
    @Override
    public Integer setInteger(@Nonnull BigInteger value) throws StreamException {
        try {
            byte sizeOfByteArray = (byte) Math.floor((value.bitLength() / 8) + 1);
            outputStreamStack.peek().write(sizeOfByteArray);
            outputStreamStack.peek().write(value.toByteArray());
        } catch (IOException e) {
            throw StreamException.with("Failed to write BigInteger value to output stream", e);
        }
        return 1;
    }
    
    @Impure
    @Override
    public Integer setDecimal32(float value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setDecimal64(double value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setString01(char value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setString64(@Nonnull @MaxSize(64) String value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setString(@Nonnull String value) throws StreamException {
        try {
            outputStreamStack.peek().writeUTF(value);
        } catch (IOException e) {
            throw StreamException.with("Failed to write string value to output stream", e);
        }
        return 1;
    }
    
    @Impure
    @Override
    public Integer setBinary128(@Nonnull @Size(16) byte[] value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setBinary256(@Nonnull @Size(32) byte[] value) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setBinary(@Nonnull byte[] value) throws StreamException {
        try {
            outputStreamStack.peek().write(value.length);
            outputStreamStack.peek().write(value);
        } catch (IOException e) {
            throw StreamException.with("Failed to write binary array to output stream", e);
        }
        return 1;
    }
    
    @Impure
    @Override
    public Integer setBinaryStream(@Nonnull InputStream stream, int length) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public <T> Integer setList(@Nonnull List<T> list, @Nonnull FailableUnaryFunction<T, Integer, StreamException> entityCollector) throws StreamException {
        try {
            // TODO: maybe add the var-length functionality here.
            outputStreamStack.peek().writeInt(list.size());
            for (T element : list) {
                entityCollector.evaluate(element);
            }
        } catch (IOException e) {
            throw StreamException.with("Failed to write list value to output stream", e);
        }
        return 1;
    }
    
    @Impure
    @Override
    public <T> Integer setArray(@Nonnull T[] value, @Nonnull FailableUnaryFunction<T, Integer, StreamException> entityCollector) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public <T> Integer setSet(@Nonnull Set<T> value, @Nonnull FailableUnaryFunction<T, Integer, StreamException> entityCollector) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public <K, V> Integer setMap(@Nonnull Map<@Nullable K, @Nullable V> value, @Nonnull FailableUnaryFunction<@Nullable K, Integer, StreamException> genericTypeKey, @Nonnull FailableUnaryFunction<@Nullable V, Integer, StreamException> genericTypeValue) throws StreamException {
        return null;
    }
    
    @Impure
    @Override
    public Integer setNull(@Nonnull CustomType customType) throws StreamException {
        return null;
    }
    
    /* -------------------------------------------------- Transformational Types -------------------------------------------------- */
    
    @Impure
    @Override
    public void setEncryptionCipher(@Nonnull Cipher cipher) {
        outputStreamStack.addAndWrapStackEntry(new CipherOutputStream(new NonClosingOutputStream(outputStreamStack.peek()), cipher));
    }
    
    @Impure
    @Override
    public @Nonnull CipherOutputStream popEncryptionCipher() throws StreamException {
        final @Nonnull CipherOutputStream cipherOutputStream = outputStreamStack.popWrappedStackEntry(CipherOutputStream.class);
        
        try {
            cipherOutputStream.close();
        } catch (IOException e) {
            throw StreamException.with("Failed to close cipher output stream", e);
        }
        return cipherOutputStream;
    }
    
    @Impure
    @Override
    public void setSignatureDigest(@Nonnull MessageDigest digest) {
        outputStreamStack.addAndWrapStackEntry(new DigestOutputStream(outputStreamStack.peek(), digest));
    }
    
    @Impure
    @Override
    public @Nonnull DigestOutputStream popSignatureDigest() {
        return outputStreamStack.popWrappedStackEntry(DigestOutputStream.class);
    }
    
    @Impure
    @Override
    public void setCompression(@Nonnull Deflater deflater) {
        outputStreamStack.addAndWrapStackEntry(new DeflaterOutputStream(outputStreamStack.peek(), deflater));
    }
    
    @Impure
    @Override
    public @Nonnull DeflaterOutputStream popCompression() throws StreamException {
        final @Nonnull DeflaterOutputStream deflaterOutputStream = outputStreamStack.popWrappedStackEntry(DeflaterOutputStream.class);
        try {
            deflaterOutputStream.finish();
        } catch (IOException e) {
            throw StreamException.with("Failed to finish deflater output stream.", e);
        }
        return deflaterOutputStream;
    }
    
}
