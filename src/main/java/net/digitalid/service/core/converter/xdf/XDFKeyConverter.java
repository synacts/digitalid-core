package net.digitalid.service.core.converter.xdf;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.utility.system.converter.TypeToTypeMapper;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;

/**
 * Converts an object to a convertible type before converting it into and from an XDF block.
 */
public class XDFKeyConverter<T, F> extends XDFConverter<Object> {

    /**
     * The type-to-type mapper.
     */
    private final @Nonnull TypeToTypeMapper<T, F> mapper;

    /**
     * Initializes the XDFKeyconverter with the type-to-type mapper.
     * 
     * @param mapper the type-to-type mapper.
     */
    private XDFKeyConverter(@Nonnull TypeToTypeMapper<T, F> mapper) {
        this.mapper = mapper;
    }

    /**
     * Returns an XDFKeyConverter with the given type-to-type mapper.
     * 
     * @param mapper The type-to-type mapper.
     * @param <T> The type to which the actual type is converted to.
     * @param <F> The type from which the convertible type is converted from.
     *
     * @return an XDFKeyConverter with the given type-to-type mapper.
     */
    public static <T, F> XDFKeyConverter get(@Nonnull TypeToTypeMapper<T, F> mapper) {
        return new XDFKeyConverter<>(mapper);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull Object convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> actualType, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        Class<T> type = mapper.getMapType();
        XDFConverter<?> converter = getRestoringTypeConverter(type);
        T object = (T) converter.convertFromNonNullable(block, type, metaData);
        return mapper.mapFrom(object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object value, @Nonnull Class<?> actualType, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        T mappedValue = mapper.mapTo((F) value);
        Class<?> type = mappedValue.getClass();
        XDFConverter converter = getStoringTypeConverter(type);
        return converter.convertToNonNullable(mappedValue, type, fieldName, parentName, metaData);
    }
}
