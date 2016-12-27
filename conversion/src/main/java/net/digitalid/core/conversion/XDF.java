package net.digitalid.core.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.exceptions.UnexpectedFailureException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Utility;


/**
 * This utility class helps converting and recovering objects to and from XDF.
 */
@Utility
public abstract class XDF {
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    public static <T> void convert(@NonCaptured @Unmodified @Nullable T object, @Nonnull Converter<T, ?> converter, @NonCaptured @Modified @Nonnull OutputStream outputStream) throws ExternalException {
        final @Nonnull XDFEncoder encoder = XDFEncoder.with(outputStream);
        converter.convert(object, encoder);
        encoder.finish();
    }
    
    @Pure
    public static <T> @Capturable @Nonnull byte[] convert(@NonCaptured @Unmodified @Nullable T object, @Nonnull Converter<T, ?> converter) throws ExternalException {
        final @Nonnull ByteArrayOutputStream output = new ByteArrayOutputStream();
        XDF.convert(object, converter, output);
        return output.toByteArray();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    public static @Capturable <T, E> @Nullable T recover(@Nonnull Converter<T, E> converter, E externallyProvided, @NonCaptured @Modified @Nonnull InputStream inputStream) throws ExternalException {
        final @Nonnull XDFDecoder decoder = XDFDecoder.with(inputStream);
        return converter.recover(decoder, externallyProvided);
    }
    
    @Pure
    public static @Capturable <T, E> @Nullable T recover(@Nonnull Converter<T, E> converter, E externallyProvided, @NonCaptured @Unmodified @Nonnull byte[] bytes) throws ExternalException {
        return XDF.recover(converter, externallyProvided, new ByteArrayInputStream(bytes));
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    private static final @Nonnull OutputStream NULL_OUTPUT_STREAM = new OutputStream() { @Override public void write(int b) {} };
    
    @Pure
    public static <T> @Nonnull @Size(32) byte[] hash(@NonCaptured @Unmodified @Nullable T object, @Nonnull Converter<T, ?> converter) throws ExternalException {
        try {
            final @Nonnull MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final @Nonnull DigestOutputStream output = new DigestOutputStream(NULL_OUTPUT_STREAM, digest);
            XDF.convert(object, converter, output);
            return digest.digest();
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw UnexpectedFailureException.with(exception);
        }
    }
    
}
