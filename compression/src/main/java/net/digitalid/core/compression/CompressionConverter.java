package net.digitalid.core.compression;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.Decoder;
import net.digitalid.utility.conversion.converter.Encoder;
import net.digitalid.utility.conversion.converter.Representation;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.string.DomainName;

/**
 * 
 * @see Deflater
 * @see Inflater
 */
@TODO(task = "Use the @GenerateSubclass mechanism to simplify this class.", date = "2016-12-27", author = Author.KASPAR_ETTER)
public class CompressionConverter<@Unspecifiable TYPE> implements Converter<Compression<TYPE>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<TYPE, ?> objectConverter;
    
    private final int deflaterMode;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private CompressionConverter(@Nonnull Converter<TYPE, ?> objectConverter, int deflaterMode) {
        this.objectConverter = objectConverter;
        this.deflaterMode = deflaterMode;
    }
    
    @Pure
    public static <T> @Nonnull CompressionConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter) {
        return new CompressionConverter<>(objectConverter, Deflater.DEFAULT_COMPRESSION);
    }
    
    @Pure
    public static <T> @Nonnull CompressionConverter<T> getInstance(@Nonnull Converter<T, ?> objectConverter, int deflaterMode) {
        return new CompressionConverter<>(objectConverter, deflaterMode);
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super Compression<TYPE>> getType() {
        return Compression.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getTypeName() {
        return "Compression";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.compression";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        return objectConverter.getFields(representation);
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified Compression<TYPE> compression, @Nonnull @Modified @NonCaptured Encoder<X> encoder) throws ExternalException {
        encoder.setCompression(new Deflater(deflaterMode));
        objectConverter.convert(compression == null ? null : compression.getObject(), encoder);
        encoder.popCompression();
        return 1;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> @Nullable Compression<TYPE> recover(@Nonnull @Modified @NonCaptured Decoder<X> decoder, @Nullable Void externallyProvided) throws ExternalException {
        decoder.setDecompression(new Inflater());
        final @Nullable TYPE object = objectConverter.recover(decoder, null);
        final Compression<@Nullable TYPE> compression = CompressionBuilder.withObject(object).build();
        decoder.popDecompression();
        return compression;
    }
    
}
