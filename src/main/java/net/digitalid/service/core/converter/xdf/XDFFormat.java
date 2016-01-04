package net.digitalid.service.core.converter.xdf;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import net.digitalid.service.core.converter.xdf.serializer.iterable.XDFArrayConverter;
import net.digitalid.service.core.converter.xdf.serializer.iterable.XDFCollectionConverter;
import net.digitalid.service.core.converter.xdf.serializer.iterable.XDFMapConverter;
import net.digitalid.service.core.converter.xdf.serializer.structure.XDFSingleFieldConverter;
import net.digitalid.service.core.converter.xdf.serializer.structure.XDFTupleConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.XDFBooleanConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.XDFObjectConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.binary.XDFBinaryConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger08Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger16Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger32Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFInteger64Converter;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.XDFIntegerConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.string.XDFCharacterConverter;
import net.digitalid.service.core.converter.xdf.serializer.value.string.XDFStringConverter;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.conversion.Convertible;
import net.digitalid.utility.conversion.Format;
import net.digitalid.utility.conversion.TypeMapper;

/**
 * Description.
 */
@Stateless
public class XDFFormat extends Format<XDFConverter<?>> {
    
    /* -------------------------------------------------- Type Converters -------------------------------------------------- */

    private final static XDFConverter<Boolean> BOOLEAN_CONVERTER = new XDFBooleanConverter();
    
    private final static XDFConverter<String> STRING_CONVERTER = new XDFStringConverter();
    
    private final static XDFConverter<Character> CHARACTER_CONVERTER = new XDFCharacterConverter();
    
    private final static XDFConverter<Byte[]> BINARY_CONVERTER = new XDFBinaryConverter();

    private final static XDFConverter<Byte> INTEGER08_CONVERTER = new XDFInteger08Converter();
    private final static XDFConverter<Short> INTEGER16_CONVERTER = new XDFInteger16Converter();
    private final static XDFConverter<Integer> INTEGER32_CONVERTER = new XDFInteger32Converter();
    private final static XDFConverter<Long> INTEGER64_CONVERTER = new XDFInteger64Converter();
    
    private final static XDFConverter<BigInteger> INTEGER_CONVERTER = new XDFIntegerConverter();
    
    private final static XDFConverter<Convertible> CONVERTIBLE_CONVERTER = new XDFObjectConverter();
    private final static XDFConverter<? extends Collection<?>> COLLECTION_CONVERTER = new XDFCollectionConverter<>();
    private final static XDFConverter<? extends Object[]> ARRAY_CONVERTER = new XDFArrayConverter();
    private final static XDFConverter<? extends Map<?, ?>> MAP_CONVERTER = new XDFMapConverter<>();
    
    // TODO: Introduce getters for the following two converters.
    private final static XDFConverter<? extends Convertible> TUPLE_CONVERTER = new XDFTupleConverter<>();
    private final static XDFConverter<? extends Convertible> SINGLE_FIELD_CONVERTER = new XDFSingleFieldConverter<>();

    @Override
    protected XDFConverter<Boolean> getBooleanConverter() {
        return BOOLEAN_CONVERTER;
    }

    @Override
    protected XDFConverter<Character> getCharacterConverter() {
        return CHARACTER_CONVERTER;
    }

    @Override
    protected XDFConverter<String> getStringConverter() {
        return STRING_CONVERTER;
    }

    @Override
    protected XDFConverter<Byte[]> getBinaryConverter() {
        return BINARY_CONVERTER;
    }

    @Override
    protected XDFConverter<BigInteger> getIntegerConverter() {
        return INTEGER_CONVERTER;
    }

    @Override
    protected XDFConverter<Byte> getInteger08Converter() {
        return INTEGER08_CONVERTER;
    }

    @Override
    protected XDFConverter<Short> getInteger16Converter() {
        return INTEGER16_CONVERTER;
    }

    @Override
    protected XDFConverter<Integer> getInteger32Converter() {
        return INTEGER32_CONVERTER;
    }

    @Override
    protected XDFConverter<Long> getInteger64Converter() {
        return INTEGER64_CONVERTER;
    }
    
    @Override
    protected XDFConverter<Convertible> getConvertibleConverter() {
        return CONVERTIBLE_CONVERTER;
    }

    @Override
    protected XDFConverter<? extends Collection<?>> getCollectionConverter() {
        return COLLECTION_CONVERTER;
    }

    @Override
    protected XDFConverter<? extends Object[]> getArrayConverter() {
        return ARRAY_CONVERTER;
    }
    
    @Override
    protected XDFConverter<? extends Map<?, ?>> getMapConverter() {
        return MAP_CONVERTER;
    }

    @Override
    protected XDFConverter<?> getTypeConverter(TypeMapper<?, ?> typeToTypeMapper) {
        return XDFKeyConverter.get(typeToTypeMapper);
    }

}
