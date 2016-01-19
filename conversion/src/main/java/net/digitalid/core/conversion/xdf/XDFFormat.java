package net.digitalid.core.conversion.xdf;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.Convertible;
import net.digitalid.utility.conversion.Format;
import net.digitalid.utility.conversion.TypeMapper;
import net.digitalid.utility.validation.state.Stateless;

import net.digitalid.core.conversion.xdf.serializer.iterable.XDFArrayConverter;
import net.digitalid.core.conversion.xdf.serializer.iterable.XDFCollectionConverter;
import net.digitalid.core.conversion.xdf.serializer.iterable.XDFMapConverter;

import net.digitalid.core.conversion.xdf.serializer.structure.XDFSingleFieldConverter;
import net.digitalid.core.conversion.xdf.serializer.structure.XDFTupleConverter;

import net.digitalid.core.conversion.xdf.serializer.value.XDFBooleanConverter;
import net.digitalid.core.conversion.xdf.serializer.value.XDFObjectConverter;

import net.digitalid.core.conversion.xdf.serializer.value.binary.XDFBinaryConverter;

import net.digitalid.core.conversion.xdf.serializer.value.integer.XDFInteger08Converter;
import net.digitalid.core.conversion.xdf.serializer.value.integer.XDFInteger16Converter;
import net.digitalid.core.conversion.xdf.serializer.value.integer.XDFInteger32Converter;
import net.digitalid.core.conversion.xdf.serializer.value.integer.XDFInteger64Converter;
import net.digitalid.core.conversion.xdf.serializer.value.integer.XDFIntegerConverter;

import net.digitalid.core.conversion.xdf.serializer.value.string.XDFCharacterConverter;
import net.digitalid.core.conversion.xdf.serializer.value.string.XDFStringConverter;

/**
 * The XDF format class which holds converters to encode and decode objects into the XDF format.
 */
@Stateless
public class XDFFormat extends Format<XDFConverter<?>> {
    
    /* -------------------------------------------------- Internal Type Converters -------------------------------------------------- */
    
    /**
     * Creates a new converter which converts boolean to XDF.
     */
    private final static @Nonnull XDFConverter<Boolean> BOOLEAN_CONVERTER = new XDFBooleanConverter();
    
     /**
     * Creates a new converter which converts a byte to XDF.
     */
    private final static @Nonnull XDFConverter<Byte> INTEGER08_CONVERTER = new XDFInteger08Converter();
    
    /**
     * Creates a new converter which converts a short to XDF.
     */
    private final static @Nonnull XDFConverter<Short> INTEGER16_CONVERTER = new XDFInteger16Converter();
    
    /**
     * Creates a new converter which converts an integer to XDF.
     */
    private final static @Nonnull XDFConverter<Integer> INTEGER32_CONVERTER = new XDFInteger32Converter();
    
    /**
     * Creates a new converter which converts a long to XDF.
     */
    private final static @Nonnull XDFConverter<Long> INTEGER64_CONVERTER = new XDFInteger64Converter();
    
    /**
     * Creates a new converter which converts a BigInteger to XDF.
     */
    private final static @Nonnull XDFConverter<BigInteger> INTEGER_CONVERTER = new XDFIntegerConverter();
    
    /**
     * Creates a new converter which converts string to XDF.
     */
    private final static @Nonnull XDFConverter<String> STRING_CONVERTER = new XDFStringConverter();
    
    /**
     * Creates a new converter which converts a character to XDF.
     */
    private final static @Nonnull XDFConverter<Character> CHARACTER_CONVERTER = new XDFCharacterConverter();
    
    /**
     * Creates a new converter which converts a byte array to XDF.
     */
    private final static @Nonnull XDFConverter<Byte[]> BINARY_CONVERTER = new XDFBinaryConverter();
    
    /**
     * Creates a new converter which converts a convertible object to XDF.
     */
    private final static @Nonnull XDFConverter<? extends Convertible> CONVERTIBLE_CONVERTER = new XDFObjectConverter<>();
    
    /**
     * Creates a new converter which converts a collection to XDF.
     */
    private final static @Nonnull XDFConverter<? extends Collection<?>> COLLECTION_CONVERTER = new XDFCollectionConverter<>();
    
    /**
     * Creates a new converter which converts an array to XDF.
     */
    private final static @Nonnull XDFConverter<? extends Object[]> ARRAY_CONVERTER = new XDFArrayConverter();
    
    /**
     * Creates a new converter which converts a map to XDF.
     */
    private final static @Nonnull XDFConverter<? extends Map<?, ?>> MAP_CONVERTER = new XDFMapConverter<>();
    
    /* -------------------------------------------------- Public Type Converters -------------------------------------------------- */
    
    /**
     * Creates a new converter which converts a tuple to XDF.
     */
    public final static @Nonnull XDFConverter<? extends Convertible> TUPLE_CONVERTER = new XDFTupleConverter<>();
    
    /**
     * Creates a new converter which converts a convertible with a single field to XDF.
     */
    public final static @Nonnull XDFConverter<? extends Convertible> SINGLE_FIELD_CONVERTER = new XDFSingleFieldConverter<>();
    
    @Override
    protected @Nonnull XDFConverter<Boolean> getBooleanConverter() {
        return BOOLEAN_CONVERTER;
    }
    
     @Override
    protected @Nonnull XDFConverter<BigInteger> getIntegerConverter() {
        return INTEGER_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<Byte> getInteger08Converter() {
        return INTEGER08_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<Short> getInteger16Converter() {
        return INTEGER16_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<Integer> getInteger32Converter() {
        return INTEGER32_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<Long> getInteger64Converter() {
        return INTEGER64_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<Character> getCharacterConverter() {
        return CHARACTER_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<String> getStringConverter() {
        return STRING_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<Byte[]> getBinaryConverter() {
        return BINARY_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<? extends Convertible> getConvertibleConverter() {
        return CONVERTIBLE_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<? extends Collection<?>> getCollectionConverter() {
        return COLLECTION_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<? extends Object[]> getArrayConverter() {
        return ARRAY_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<? extends Map<?, ?>> getMapConverter() {
        return MAP_CONVERTER;
    }
    
    @Override
    protected @Nonnull XDFConverter<?> getTypeConverter(@Nonnull TypeMapper<?, ?> typeToTypeMapper) {
        return XDFKeyConverter.get(typeToTypeMapper);
    }
    
}
