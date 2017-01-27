package net.digitalid.core.conversion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.errors.SupportErrorBuilder;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.conversion.encoders.FileEncoder;
import net.digitalid.core.conversion.encoders.MemoryEncoder;
import net.digitalid.core.conversion.encoders.NetworkEncoder;
import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.conversion.exceptions.MemoryException;
import net.digitalid.core.conversion.exceptions.NetworkException;

/**
 * This utility class helps converting and recovering objects to and from XDF.
 */
@Utility
public abstract class XDF {
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    /**
     * Returns the given object converted with the given converter as a byte array.
     */
    @Pure
    public static <@Unspecifiable TYPE> @Capturable @Nonnull byte[] convert(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object) {
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
            encoder.encodeObject(converter, object);
        } catch (@Nonnull MemoryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
        return outputStream.toByteArray();
    }
    
    /**
     * Converts the given object with the given converter to the given file.
     */
    @Pure
    public static <@Unspecifiable TYPE> void convert(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object, @Nonnull File file) throws FileException {
        try (@Nonnull FileEncoder encoder = FileEncoder.of(file)) {
            encoder.encodeObject(converter, object);
        }
    }
    
    /**
     * Converts the given object with the given converter to the given socket.
     */
    @Pure
    public static <@Unspecifiable TYPE> void convert(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object, @Nonnull Socket socket) throws NetworkException {
        try (@Nonnull NetworkEncoder encoder = NetworkEncoder.of(socket)) {
            encoder.encodeObject(converter, object);
        }
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
//    @Pure
//    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nullable TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, PROVIDED externallyProvided, @NonCaptured @Modified @Nonnull InputStream inputStream) throws ExternalException {
//        final @Nonnull XDFDecoder decoder = XDFDecoder.with(inputStream);
//        return converter.recover(decoder, externallyProvided);
//    }
//    
//    @Pure
//    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nullable TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, PROVIDED externallyProvided, @NonCaptured @Unmodified @Nonnull byte[] bytes) throws ExternalException {
//        return XDF.recover(converter, externallyProvided, new ByteArrayInputStream(bytes));
//    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    /**
     * Stores an output stream that ignores all input.
     */
    public static final @Nonnull OutputStream NULL_OUTPUT_STREAM = new OutputStream() { @Override public void write(int b) {} };
    
    /**
     * Returns the hash of the given object converted with the given converter.
     */
    @Pure
    public static <@Unspecifiable TYPE> @Nonnull @Size(32) byte[] hash(@Nonnull Converter<TYPE, ?> converter, @NonCaptured @Unmodified @Nonnull TYPE object) {
        try {
            final @Nonnull MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            final @Nonnull DigestOutputStream outputStream = new DigestOutputStream(NULL_OUTPUT_STREAM, messageDigest);
            try (@Nonnull MemoryEncoder encoder = MemoryEncoder.of(outputStream)) {
                encoder.encodeObject(converter, object);
            } catch (@Nonnull MemoryException exception) {
                throw UncheckedExceptionBuilder.withCause(exception).build();
            }
            return messageDigest.digest();
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw SupportErrorBuilder.withMessage("The message digest 'SHA-256' is not available.").withCause(exception).build();
        }
    }
    
}
