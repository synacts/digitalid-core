package net.digitalid.service.core.converter.xdf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.TypeMapper;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * Converts an object to a convertible type before converting it into and from an XDF block.
 */
public class XDFKeyConverter<F, T> extends XDFConverter<Object> {
    
    /**
     * The mapper which converts and recovers an object from one type into another.
     */
    private final @Nonnull TypeMapper<F, T> mapper;
    
    /**
     * Initializes the XDFKeyconverter with the type mapper.
     * 
     * @param mapper the type mapper.
     */
    private XDFKeyConverter(@Nonnull TypeMapper<F, T> mapper) {
        this.mapper = mapper;
    }
    
    /**
     * Returns an XDFKeyConverter with the given type mapper.
     * 
     * @param mapper The type mapper.
     * @param <F> The type from which the convertible type is converted from.
     * @param <T> The type to which the actual type is converted to.
     *
     * @return an XDFKeyConverter with the given type mapper.
     */
    public static <T, F> XDFKeyConverter get(@Nonnull TypeMapper<F, T> mapper) {
        return new XDFKeyConverter<>(mapper);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull Object recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> actualType, @Nonnull ConverterAnnotations metaData) throws RecoveryException, InternalException, InvalidEncodingException {
        final @Nonnull Class<T> type = mapper.getMapType();
        final @Nonnull XDFConverter<?> converter = XDF.FORMAT.getConverter(type);
        final @Nonnull T object = (T) converter.recoverNonNullable(block, type, metaData);
        return mapper.recover(object);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object value, @Nonnull Class<?> actualType, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException, InternalException {
        final @Nonnull T mappedValue = mapper.convert((F) value);
        final @Nonnull Class<T> type = mapper.getMapType();
        final @Nonnull XDFConverter converter = XDF.FORMAT.getConverter(type);
        return converter.convertNonNullable(mappedValue, type, fieldName, parentName, metaData);
    }
    
}
