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

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.conversion.encoders.XDFEncoder;


/**
 * This utility class helps converting and recovering objects to and from XDF.
 */
@Utility
public abstract class XDF {
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    public static <@Unspecifiable TYPE> void convert(@NonCaptured @Unmodified @Nullable TYPE object, @Nonnull Converter<TYPE, ?> converter, @NonCaptured @Modified @Nonnull OutputStream outputStream) throws ExternalException {
        final @Nonnull XDFEncoder encoder = XDFEncoder.with(outputStream);
        converter.convert(object, encoder);
        encoder.finish();
    }
    
    @Pure
    public static <@Unspecifiable TYPE> @Capturable @Nonnull byte[] convert(@NonCaptured @Unmodified @Nullable TYPE object, @Nonnull Converter<TYPE, ?> converter) throws ExternalException {
        final @Nonnull ByteArrayOutputStream output = new ByteArrayOutputStream();
        XDF.convert(object, converter, output);
        return output.toByteArray();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nullable TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, PROVIDED externallyProvided, @NonCaptured @Modified @Nonnull InputStream inputStream) throws ExternalException {
        final @Nonnull XDFDecoder decoder = XDFDecoder.with(inputStream);
        return converter.recover(decoder, externallyProvided);
    }
    
    @Pure
    public static @Capturable <@Unspecifiable TYPE, @Specifiable PROVIDED> @Nullable TYPE recover(@Nonnull Converter<TYPE, PROVIDED> converter, PROVIDED externallyProvided, @NonCaptured @Unmodified @Nonnull byte[] bytes) throws ExternalException {
        return XDF.recover(converter, externallyProvided, new ByteArrayInputStream(bytes));
    }
    
    /* -------------------------------------------------- Hashing -------------------------------------------------- */
    
    private static final @Nonnull OutputStream NULL_OUTPUT_STREAM = new OutputStream() { @Override public void write(int b) {} };
    
    @Pure
    public static <@Unspecifiable TYPE> @Nonnull @Size(32) byte[] hash(@NonCaptured @Unmodified @Nullable TYPE object, @Nonnull Converter<TYPE, ?> converter) throws ExternalException {
        try {
            final @Nonnull MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final @Nonnull DigestOutputStream output = new DigestOutputStream(NULL_OUTPUT_STREAM, digest);
            XDF.convert(object, converter, output);
            return digest.digest();
        } catch (@Nonnull NoSuchAlgorithmException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
}
