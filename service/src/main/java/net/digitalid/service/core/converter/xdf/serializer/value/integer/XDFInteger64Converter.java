package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;

public class XDFInteger64Converter extends XDFConverter<Long> {
    
    @Override
    protected @Nonnull Long recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return Integer64Wrapper.decode(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        assert (object instanceof Long) : "The object is an instance of the long type.";

        final long value = (Long) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, Integer64Wrapper.XDF_TYPE);
        return Integer64Wrapper.encode(semanticType, value);
    }
    
}
