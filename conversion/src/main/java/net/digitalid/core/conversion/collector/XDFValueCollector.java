package net.digitalid.core.conversion.collector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.converter.ValueCollectorImplementation;
import net.digitalid.utility.conversion.converter.types.CustomType;
import net.digitalid.utility.functional.failable.FailableUnaryFunction;
import net.digitalid.utility.logging.exceptions.io.StreamException;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.Size;

import net.digitalid.core.cryptography.encryption.NonClosingOutputStream;

/**
 *
 */
public class XDFValueCollector extends ValueCollectorImplementation<StreamException> {
    
    /* -------------------------------------------------- Final Fields -------------------------------------------------- */
    
    /**
     * The output stream into which we are writing.
     */
    private @Nonnull Stack<@Nonnull OutputStream> outputStreamStack;
    
    @Pure
    public @Nonnull OutputStream getOutputStream() {
        return outputStreamStack.peek();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new XDF value collector instance.
     */
    private XDFValueCollector(@Nonnull OutputStream outputStream) {
        this.outputStreamStack = new Stack<>();
        outputStreamStack.add(outputStream);
    }
    
    /**
     * Returns an XDF value collector for a given output stream.
     */
    @Pure
    public static @Nonnull XDFValueCollector with(@Nonnull OutputStream outputStream) {
        return new XDFValueCollector(outputStream);
    }
    
    /* -------------------------------------------------- Overridden Collector Methods -------------------------------------------------- */
       
    @Impure
    @Override
    public Integer setEmpty() {
        // TODO: unclear if this should be supported. What's the difference to null?
        return null;
    }
    
    @Impure
    @Override
    public Integer setBoolean(boolean value) throws StreamException {
        try {
            getOutputStream().write(value ? 1 : 0);
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
            long shifter = value;
            for (int i = 7; i >= 0; i--) {
                getOutputStream().write((byte) shifter);
                shifter >>>= 8;
            }
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
            getOutputStream().write(sizeOfByteArray);
            getOutputStream().write(value.toByteArray());
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
            getOutputStream().write(value.getBytes("UTF-16BE"));
            getOutputStream().write(0);
            getOutputStream().write(0);
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
            getOutputStream().write(value.length);
            getOutputStream().write(value);
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
            getOutputStream().write(list.size());
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
    
    @Impure
    @Override
    public void setEncryptionCipher(@Nonnull Cipher cipher) {
        outputStreamStack.add(new CipherOutputStream(new NonClosingOutputStream(getOutputStream()), cipher));
    }
    
    @Impure
    @Override
    public @Nonnull CipherOutputStream popEncryptionCipher() throws StreamException {
        Require.that(getOutputStream() instanceof CipherOutputStream).orThrow("Cipher output stream not found.");
    
        try {
            getOutputStream().close();
        } catch (IOException e) {
            throw StreamException.with("Failed to close cipher output stream", e);
        }
        return (CipherOutputStream) outputStreamStack.pop();
    }
    
    @Impure
    @Override
    public void setSignatureDigest(@Nonnull MessageDigest digest) {
        outputStreamStack.add(new DigestOutputStream(getOutputStream(), digest));
    }
    
    @Impure
    @Override
    public @Nullable DigestOutputStream popSignatureDigest() {
        Require.that(getOutputStream() instanceof DigestOutputStream).orThrow("Signature digest output stream not found.");
        
        return (DigestOutputStream) outputStreamStack.pop();
    }
    
    @Impure
    @Override
    public void setCompression(@Nonnull Deflater deflater) {
        outputStreamStack.add(new DeflaterOutputStream(getOutputStream(), deflater));
    }
    
    @Impure
    @Override
    public @Nonnull DeflaterOutputStream popCompression() throws StreamException {
        Require.that(getOutputStream() instanceof DeflaterOutputStream).orThrow("Deflater output stream not found.");
    
        final @Nonnull DeflaterOutputStream deflaterOutputStream = (DeflaterOutputStream) outputStreamStack.pop();
        try {
            deflaterOutputStream.finish();
        } catch (IOException e) {
            throw StreamException.with("Failed to finish deflater output stream.", e);
        }
        return deflaterOutputStream;
    }
    
}
