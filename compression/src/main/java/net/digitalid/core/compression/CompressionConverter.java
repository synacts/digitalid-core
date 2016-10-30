package net.digitalid.core.compression;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.conversion.converter.ValueCollector;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;

import net.digitalid.core.cryptography.compression.CompressionBuilder;

/**
 *
 * @see Deflater
 * @see Inflater
 */
public class CompressionConverter<T> implements Converter<Compression<T>, Void> {
    
    /* -------------------------------------------------- Object Converter -------------------------------------------------- */
    
    private final @Nonnull Converter<T, ?> objectConverter;
    
    private final int deflaterMode;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private CompressionConverter(@Nonnull Converter<T, ?> objectConverter, int deflaterMode) {
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
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return "compression";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ImmutableList<CustomField> getFields() {
        return objectConverter.getFields();
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified Compression<T> compression, @Nonnull @Modified @NonCaptured ValueCollector<X> valueCollector) throws ExternalException {
        valueCollector.setCompression(new Deflater(deflaterMode));
        objectConverter.convert(compression == null ? null : compression.getObject(), valueCollector);
        valueCollector.popCompression();
        return 1;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> @Nullable Compression<T> recover(@Nonnull @Modified @NonCaptured SelectionResult<X> selectionResult, @Nullable Void externallyProvided) throws ExternalException {
        selectionResult.setDecompression(new Inflater());
        final @Nullable T object = objectConverter.recover(selectionResult, null);
        final Compression<@Nullable T> compression = CompressionBuilder.withObject(object).build();
        selectionResult.popDecompression();
        return compression;
    }
    
}
