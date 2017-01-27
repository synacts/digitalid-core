package net.digitalid.core.conversion.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.validation.annotations.method.Ensures;
import net.digitalid.utility.validation.annotations.method.Requires;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.StreamException;
import net.digitalid.core.conversion.streams.output.NonClosingOutputStreamBuilder;
import net.digitalid.core.conversion.streams.output.WrappedOutputStream;
import net.digitalid.core.conversion.streams.output.WrappedOutputStreamBuilder;

/**
 * An XDF encoder encodes values as XDF to an output stream.
 */
@Mutable
public abstract class XDFEncoder<@Unspecifiable EXCEPTION extends StreamException> implements Encoder<EXCEPTION> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    /**
     * Returns the given IO exception wrapped as the parameterized exception type.
     */
    @Pure
    protected abstract @Nonnull EXCEPTION createException(@Nonnull IOException exception);
    
    /* -------------------------------------------------- Stream -------------------------------------------------- */
    
    protected @Nonnull WrappedOutputStream outputStream;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected XDFEncoder(@Nonnull OutputStream outputStream) {
        this.outputStream = WrappedOutputStreamBuilder.withWrappedStream(outputStream).build();
    }
    
    /* -------------------------------------------------- Representation -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Representation getRepresentation() {
        return Representation.EXTERNAL;
    }
    
    /* -------------------------------------------------- Objects -------------------------------------------------- */
    
    @Impure
    @Override
    public <@Unspecifiable TYPE> void encodeObject(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object) throws EXCEPTION {
        // TODO: Handle the hierarchy here!
        converter.convert(object, this);
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE> void encodeNullableObject(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nullable TYPE object) throws EXCEPTION {
        encodeBoolean(object != null);
        if (object != null) { encodeObject(converter, object); }
    }
    
    /* -------------------------------------------------- Values -------------------------------------------------- */
    
    @Impure
    @Override
    public void encodeBoolean(boolean value) throws EXCEPTION {
        try { outputStream.writeBoolean(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeInteger08(byte value) throws EXCEPTION {
        try { outputStream.writeByte(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeInteger16(short value) throws EXCEPTION {
        try { outputStream.writeShort(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeInteger32(int value) throws EXCEPTION {
        try { outputStream.writeInt(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeInteger64(long value) throws EXCEPTION {
        try { outputStream.writeLong(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeInteger(@Nonnull BigInteger value) throws EXCEPTION {
        encodeBinary(value.toByteArray());
    }
    
    @Impure
    @Override
    public void encodeDecimal32(float value) throws EXCEPTION {
        try { outputStream.writeFloat(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeDecimal64(double value) throws EXCEPTION {
        try { outputStream.writeDouble(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeString01(char value) throws EXCEPTION {
        try { outputStream.writeChar(value); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeString64(@Nonnull @MaxSize(64) String string) throws EXCEPTION {
        try { outputStream.writeUTF(string); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeString(@Nonnull String string) throws EXCEPTION {
        try { outputStream.writeUTF(string); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeBinary128(@Nonnull @Size(16) byte[] bytes) throws EXCEPTION {
        try { outputStream.write(bytes); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeBinary256(@Nonnull @Size(32) byte[] bytes) throws EXCEPTION {
        try { outputStream.write(bytes); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeBinary(@Nonnull byte[] bytes) throws EXCEPTION {
        try { outputStream.writeInt(bytes.length); outputStream.write(bytes); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public void encodeBinaryStream(@Nonnull InputStream inputStream, int length) throws EXCEPTION {
        // TODO: Benchmark whether the commented code is faster.
//        try {
//            outputStream.writeInt(length);
//            for (int i = 0; i < length; i++) {
//                outputStream.write(inputStream.read());
//
//            }
//        } catch (@Nonnull IOException exception) {
//            throw createException(exception);
//        }
        final @Nonnull byte[] buffer = new byte[length];
        int offset = 0;
        try {
            do {
                final int read = inputStream.read(buffer, offset, length - offset);
                if (read == -1) { throw new IOException("Reached the end of the input stream before the length " + length + "."); }
                else { offset += read; }
            } while (offset < length);
        } catch (@Nonnull IOException exception) {
            throw createException(exception);
        }
        encodeBinary(buffer);
    }
    
    /* -------------------------------------------------- Collections -------------------------------------------------- */
    
    @Impure
    @Override
    public <@Unspecifiable TYPE> void encodeOrderedIterable(@Nonnull Converter<TYPE, ?> converter, @Nonnull FiniteIterable<@Nonnull TYPE> iterable) throws EXCEPTION {
        encodeInteger32(iterable.size());
        for (@Nonnull TYPE element : iterable) {
            encodeObject(converter, element);
        }
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE> void encodeOrderedIterableWithNullableElements(@Nonnull Converter<TYPE, ?> converter, @Nonnull FiniteIterable<@Nullable TYPE> iterable) throws EXCEPTION {
        encodeInteger32(iterable.size());
        for (@Nullable TYPE element : iterable) {
            encodeNullableObject(converter, element);
        }
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE> void encodeUnorderedIterable(@Nonnull Converter<TYPE, ?> converter, @Nonnull FiniteIterable<@Nonnull TYPE> iterable) throws EXCEPTION {
        encodeOrderedIterable(converter, iterable);
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE> void encodeUnorderedIterableWithNullableElements(@Nonnull Converter<TYPE, ?> converter, @Nonnull FiniteIterable<@Nullable TYPE> iterable) throws EXCEPTION {
        encodeOrderedIterableWithNullableElements(converter, iterable);
    }
    
    @Impure
    @Override
    public <@Unspecifiable KEY, @Unspecifiable VALUE> void encodeMap(@Nonnull Converter<KEY, ?> keyConverter, @Nonnull Converter<VALUE, ?> valueConverter, @Nonnull Map<@Nonnull KEY, @Nonnull VALUE> map) throws EXCEPTION {
        encodeInteger32(map.size());
        for (Map.@Nonnull Entry<@Nonnull KEY, @Nonnull VALUE> entry : map.entrySet()) {
            encodeObject(keyConverter, entry.getKey());
            encodeObject(valueConverter, entry.getValue());
        }
    }
    
    @Impure
    @Override
    public <@Unspecifiable KEY, @Unspecifiable VALUE> void encodeMapWithNullableValues(@Nonnull Converter<KEY, ?> keyConverter, @Nonnull Converter<VALUE, ?> valueConverter, @Nonnull Map<@Nullable KEY, @Nullable VALUE> map) throws EXCEPTION {
        encodeInteger32(map.size());
        for (Map.@Nonnull Entry<@Nullable KEY, @Nullable VALUE> entry : map.entrySet()) {
            encodeNullableObject(keyConverter, entry.getKey());
            encodeNullableObject(valueConverter, entry.getValue());
        }
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isHashing() {
        return outputStream.wrapsInstanceOf(DigestOutputStream.class);
    }
    
    @Impure
    @Override
    @Ensures(condition = "isHashing()", message = "The encoder has to be hashing.")
    public void startHashing(@Nonnull MessageDigest digest) {
        this.outputStream = WrappedOutputStreamBuilder.withWrappedStream(new DigestOutputStream(outputStream, digest)).withPreviousStream(outputStream).build();
    }
    
    @Impure
    @Override
    @Requires(condition = "isHashing()", message = "The encoder has to be hashing.")
    public @Nonnull byte[] stopHashing() {
        final @Nonnull DigestOutputStream digestOutputStream = outputStream.getWrappedStream(DigestOutputStream.class);
        this.outputStream = outputStream.getPreviousStream(DigestOutputStream.class);
        return digestOutputStream.getMessageDigest().digest();
    }
    
    /* -------------------------------------------------- Compressing -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isCompressing() {
        return outputStream.wrapsInstanceOf(DeflaterOutputStream.class);
    }
    
    @Impure
    @Override
    @Ensures(condition = "isCompressing()", message = "The encoder has to be compressing.")
    public void startCompressing(@Nonnull Deflater deflater) {
        this.outputStream = WrappedOutputStreamBuilder.withWrappedStream(new DeflaterOutputStream(outputStream, deflater)).withPreviousStream(outputStream).build();
    }
    
    @Impure
    @Override
    @Requires(condition = "isCompressing()", message = "The encoder has to be compressing.")
    public void stopCompressing() throws EXCEPTION {
        final @Nonnull DeflaterOutputStream deflaterOutputStream = outputStream.getWrappedStream(DeflaterOutputStream.class);
        try { deflaterOutputStream.finish(); } catch (@Nonnull IOException exception) { throw createException(exception); }
        this.outputStream = outputStream.getPreviousStream(DeflaterOutputStream.class);
    }
    
    /* -------------------------------------------------- Encrypting -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isEncrypting() {
        return outputStream.wrapsInstanceOf(CipherOutputStream.class);
    }
    
    @Impure
    @Override
    @Ensures(condition = "isEncrypting()", message = "The encoder has to be encrypting.")
    public void startEncrypting(@Nonnull Cipher cipher) {
        this.outputStream = WrappedOutputStreamBuilder.withWrappedStream(new CipherOutputStream(NonClosingOutputStreamBuilder.withOutputStream(outputStream).build(), cipher)).withPreviousStream(outputStream).build();
    }
    
    @Impure
    @Override
    @Requires(condition = "isEncrypting()", message = "The encoder has to be encrypting.")
    public void stopEncrypting() throws EXCEPTION {
        final @Nonnull CipherOutputStream cipherOutputStream = outputStream.getWrappedStream(CipherOutputStream.class);
        try { cipherOutputStream.close(); } catch (@Nonnull IOException exception) { throw createException(exception); }
        this.outputStream = outputStream.getPreviousStream(CipherOutputStream.class);
    }
    
    /* -------------------------------------------------- Closing -------------------------------------------------- */
    
    @Impure
    @Override
    public void close() throws EXCEPTION {
        Require.that(!outputStream.hasPreviousStream()).orThrow("There may no longer be a previous output stream when closing an encoder.");
        
        try { outputStream.close(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
}
