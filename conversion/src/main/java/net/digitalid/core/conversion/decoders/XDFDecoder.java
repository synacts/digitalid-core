package net.digitalid.core.conversion.decoders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.ownership.Shared;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.functional.failable.FailableCollector;
import net.digitalid.utility.functional.interfaces.UnaryFunction;
import net.digitalid.utility.validation.annotations.method.Ensures;
import net.digitalid.utility.validation.annotations.method.Requires;
import net.digitalid.utility.validation.annotations.size.Empty;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.conversion.exceptions.StreamException;
import net.digitalid.core.conversion.streams.input.BufferedInflaterInputStream;
import net.digitalid.core.conversion.streams.input.CustomCipherInputStream;
import net.digitalid.core.conversion.streams.input.WrappedInputStream;
import net.digitalid.core.conversion.streams.input.WrappedInputStreamBuilder;

/**
 * An XDF encoder encodes values as XDF to an output stream.
 */
@Mutable
public abstract class XDFDecoder<@Unspecifiable EXCEPTION extends StreamException> implements Decoder<EXCEPTION> {
    
    /* -------------------------------------------------- Exception -------------------------------------------------- */
    
    /**
     * Returns the given IO exception wrapped as the parameterized exception type.
     */
    @Pure
    protected abstract @Nonnull EXCEPTION createException(@Nonnull IOException exception);
    
    /* -------------------------------------------------- Stream -------------------------------------------------- */
    
    private @Nonnull WrappedInputStream inputStream;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected XDFDecoder(@Nonnull InputStream inputStream) {
        this.inputStream = WrappedInputStreamBuilder.withWrappedStream(inputStream).build();
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
    public <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nonnull TYPE decodeObject(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided) throws EXCEPTION, RecoveryException {
        // TODO: Handle the hierarchy here!
        return converter.recover(this, provided);
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nullable TYPE decodeNullableObject(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided) throws EXCEPTION, RecoveryException {
        final boolean nonNull = decodeBoolean();
        return nonNull ? decodeObject(converter, provided) : null;
    }
    
    /* -------------------------------------------------- Values -------------------------------------------------- */
    
    @Impure
    @Override
    public boolean decodeBoolean() throws EXCEPTION {
        try { return inputStream.readBoolean(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public byte decodeInteger08() throws EXCEPTION {
        try { return inputStream.readByte(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public short decodeInteger16() throws EXCEPTION {
        try { return inputStream.readShort(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public int decodeInteger32() throws EXCEPTION {
        try { return inputStream.readInt(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public long decodeInteger64() throws EXCEPTION {
        try { return inputStream.readLong(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    @TODO(task = "Verify that negative numbers are recovered correctly.", date = "2017-01-27", author = Author.KASPAR_ETTER)
    public @Nonnull BigInteger decodeInteger() throws EXCEPTION {
        return new BigInteger(decodeBinary());
    }
    
    @Impure
    @Override
    public float decodeDecimal32() throws EXCEPTION {
        try { return inputStream.readFloat(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public double decodeDecimal64() throws EXCEPTION {
        try { return inputStream.readDouble(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public char decodeString01() throws EXCEPTION {
        try { return inputStream.readChar(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public @Nonnull @MaxSize(64) String decodeString64() throws EXCEPTION {
        try {
            final @Nonnull String string = inputStream.readUTF();
            if (string.length() > 64) {
                throw new IOException("The recovered string has a length of " + string.length() + " instead of at most 64.");
            }
            return string;
        } catch (@Nonnull IOException exception) {
            throw createException(exception);
        }
    }
    
    @Impure
    @Override
    public @Nonnull String decodeString() throws EXCEPTION {
        try { return inputStream.readUTF(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
    @Impure
    @Override
    public @Nonnull @Size(16) byte[] decodeBinary128() throws EXCEPTION {
        try {
            final @Nonnull byte[] bytes = new byte[16];
            inputStream.readFully(bytes);
            return bytes;
        } catch (@Nonnull IOException exception) {
            throw createException(exception);
        }
    }
    
    @Impure
    @Override
    public @Nonnull @Size(32) byte[] decodeBinary256() throws EXCEPTION {
        try {
            final @Nonnull byte[] bytes = new byte[32];
            inputStream.readFully(bytes);
            return bytes;
        } catch (@Nonnull IOException exception) {
            throw createException(exception);
        }
    }
    
    @Impure
    @Override
    public @Nonnull byte[] decodeBinary() throws EXCEPTION {
        try {
            final int length = inputStream.readInt();
            final @Nonnull byte[] bytes = new byte[length];
            inputStream.readFully(bytes);
            return bytes;
        } catch (@Nonnull IOException exception) {
            throw createException(exception);
        }
    }
    
    @Impure
    @Override
    public @Nonnull InputStream decodeBinaryStream() throws EXCEPTION {
        // Reading all the bytes here seems to be the only way to prevent a
        // caller from ignoring the result and continue with another method.
        try {
            final int length = inputStream.readInt();
            final @Nonnull byte[] bytes = new byte[length];
            inputStream.readFully(bytes);
            return new ByteArrayInputStream(bytes);
        } catch (@Nonnull IOException exception) {
            throw createException(exception);
        }
    }
    
    /* -------------------------------------------------- Collections -------------------------------------------------- */
    
    @Impure
    @Override
    public <@Unspecifiable TYPE, @Specifiable PROVIDED, @Unspecifiable ITERABLE, @Unspecifiable COLLECTOR extends FailableCollector<@Nonnull TYPE, @Nonnull ITERABLE, RecoveryException, RecoveryException>> @Nonnull ITERABLE decodeOrderedIterable(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @Nonnull UnaryFunction<@Nonnull Integer, @Nonnull COLLECTOR> constructor) throws EXCEPTION, RecoveryException {
        final int size = decodeInteger32();
        final @Nonnull COLLECTOR collector = constructor.evaluate(size);
        for (int i = 0; i < size; i++) {
            final @Nonnull TYPE object = decodeObject(converter, provided);
            collector.consume(object);
        }
        return collector.getResult();
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE, @Specifiable PROVIDED, @Unspecifiable ITERABLE, @Unspecifiable COLLECTOR extends FailableCollector<@Nullable TYPE, @Nonnull ITERABLE, RecoveryException, RecoveryException>> @Nonnull ITERABLE decodeOrderedIterableWithNullableElements(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @Nonnull UnaryFunction<@Nonnull Integer, @Nonnull COLLECTOR> constructor) throws EXCEPTION, RecoveryException {
        final int size = decodeInteger32();
        final @Nonnull COLLECTOR collector = constructor.evaluate(size);
        for (int i = 0; i < size; i++) {
            final @Nullable TYPE object = decodeNullableObject(converter, provided);
            collector.consume(object);
        }
        return collector.getResult();
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE, @Specifiable PROVIDED, @Unspecifiable ITERABLE, @Unspecifiable COLLECTOR extends FailableCollector<@Nonnull TYPE, @Nonnull ITERABLE, RecoveryException, RecoveryException>> @Nonnull ITERABLE decodeUnorderedIterable(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @Nonnull UnaryFunction<@Nonnull Integer, @Nonnull COLLECTOR> constructor) throws EXCEPTION, RecoveryException {
        return decodeOrderedIterable(converter, provided, constructor);
    }
    
    @Impure
    @Override
    public <@Unspecifiable TYPE, @Specifiable PROVIDED, @Unspecifiable ITERABLE, @Unspecifiable COLLECTOR extends FailableCollector<@Nullable TYPE, @Nonnull ITERABLE, RecoveryException, RecoveryException>> @Nonnull ITERABLE decodeUnorderedIterableWithNullableElements(@Nonnull Converter<TYPE, PROVIDED> converter, @Shared PROVIDED provided, @Nonnull UnaryFunction<@Nonnull Integer, @Nonnull COLLECTOR> constructor) throws EXCEPTION, RecoveryException {
        return decodeOrderedIterableWithNullableElements(converter, provided, constructor);
    }
    
    @Impure
    @Override
    public <@Unspecifiable KEY, @Specifiable PROVIDED_FOR_KEY, @Unspecifiable VALUE, @Specifiable PROVIDED_FOR_VALUE> @Nonnull Map<@Nonnull KEY, @Nonnull VALUE> decodeMap(@Nonnull Converter<KEY, PROVIDED_FOR_KEY> keyConverter, @Shared PROVIDED_FOR_KEY providedForKey, @Nonnull Converter<VALUE, PROVIDED_FOR_VALUE> valueConverter, @Shared PROVIDED_FOR_VALUE providedForValue, @NonCaptured @Modified @Nonnull @Empty Map<@Nonnull KEY, @Nonnull VALUE> emptyMap) throws EXCEPTION, RecoveryException {
        final int size = decodeInteger32();
        for (int i = 0; i < size; i++) {
            final @Nonnull KEY key = decodeObject(keyConverter, providedForKey);
            final @Nonnull VALUE value = decodeObject(valueConverter, providedForValue);
            emptyMap.put(key, value);
        }
        return emptyMap;
    }
    
    @Impure
    @Override
    public <@Unspecifiable KEY, @Specifiable PROVIDED_FOR_KEY, @Unspecifiable VALUE, @Specifiable PROVIDED_FOR_VALUE> @Nonnull Map<@Nullable KEY, @Nullable VALUE> decodeMapWithNullableValues(@Nonnull Converter<KEY, PROVIDED_FOR_KEY> keyConverter, @Shared PROVIDED_FOR_KEY providedForKey, @Nonnull Converter<VALUE, PROVIDED_FOR_VALUE> valueConverter, @Shared PROVIDED_FOR_VALUE providedForValue, @NonCaptured @Modified @Nonnull @Empty Map<@Nullable KEY, @Nullable VALUE> emptyMap) throws EXCEPTION, RecoveryException {
        final int size = decodeInteger32();
        for (int i = 0; i < size; i++) {
            final @Nullable KEY key = decodeNullableObject(keyConverter, providedForKey);
            final @Nullable VALUE value = decodeNullableObject(valueConverter, providedForValue);
            emptyMap.put(key, value);
        }
        return emptyMap;
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isHashing() {
        return inputStream.wrapsInstanceOf(DigestInputStream.class);
    }
    
    @Impure
    @Override
    @Ensures(condition = "isHashing()", message = "The decoder has to be hashing.")
    public void startHashing(@Nonnull MessageDigest digest) {
        this.inputStream = WrappedInputStreamBuilder.withWrappedStream(new DigestInputStream(inputStream, digest)).withPreviousStream(inputStream).build();
    }
    
    @Impure
    @Override
    @Requires(condition = "isHashing()", message = "The decoder has to be hashing.")
    public @Nonnull byte[] stopHashing() {
        final @Nonnull DigestInputStream digestInputStream = inputStream.getWrappedStream(DigestInputStream.class);
        this.inputStream = inputStream.getPreviousStream(DigestInputStream.class);
        return digestInputStream.getMessageDigest().digest();
    }
    
    /* -------------------------------------------------- Decompressing -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isDecompressing() {
        return inputStream.wrapsInstanceOf(BufferedInflaterInputStream.class);
    }
    
    @Impure
    @Override
    @Ensures(condition = "isDecompressing()", message = "The decoder has to be decompressing.")
    public void startDecompressing(@Nonnull Inflater inflater) {
        this.inputStream = WrappedInputStreamBuilder.withWrappedStream(new BufferedInflaterInputStream(inputStream)).withPreviousStream(inputStream).build();
    }
    
    @Impure
    @Override
    @Requires(condition = "isDecompressing()", message = "The decoder has to be decompressing.")
    public void stopDecompressing() throws EXCEPTION {
        // TODO: The following lines were like this in a previous version but seem to do nothing, so I commented them out.
//        final @Nonnull BufferedInflaterInputStream inflaterInputStream = inputStream.getWrappedStream(BufferedInflaterInputStream.class);
//        inflaterInputStream.finish();
        this.inputStream = inputStream.getPreviousStream(BufferedInflaterInputStream.class);
    }
    
    /* -------------------------------------------------- Decrypting -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isDecrypting() {
        return inputStream.wrapsInstanceOf(CustomCipherInputStream.class);
    }
    
    @Impure
    @Override
    @Ensures(condition = "isDecrypting()", message = "The decoder has to be decrypting.")
    public void startDecrypting(@Nonnull Cipher cipher) {
        this.inputStream = WrappedInputStreamBuilder.withWrappedStream(CustomCipherInputStream.with(inputStream, cipher)).withPreviousStream(inputStream).build();
    }
    
    @Impure
    @Override
    @Requires(condition = "isDecrypting()", message = "The decoder has to be decrypting.")
    public void stopDecrypting() throws EXCEPTION {
        this.inputStream = inputStream.getPreviousStream(CustomCipherInputStream.class);
    }
    
    /* -------------------------------------------------- Closing -------------------------------------------------- */
    
    @Impure
    @Override
    public void close() throws EXCEPTION {
        Require.that(!inputStream.hasPreviousStream()).orThrow("There may no longer be a previous input stream when closing a decoder.");
        
        try { inputStream.close(); } catch (@Nonnull IOException exception) { throw createException(exception); }
    }
    
}
