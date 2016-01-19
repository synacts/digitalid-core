package net.digitalid.core.conversion.xdf.serializer.value.integer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.value.integer.Integer16Wrapper;

import net.digitalid.core.conversion.xdf.XDFConverter;

import net.digitalid.core.identity.SemanticType;

public class XDFInteger16Converter extends XDFConverter<Short> {
    
    @Override
    protected @Nonnull Short recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return Integer16Wrapper.decode(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        assert (object instanceof Short) : "The object is an instance of the byte type.";

        final short value = (Short) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, Integer16Wrapper.XDF_TYPE);
        return Integer16Wrapper.encode(semanticType, value);
    }
    
}
