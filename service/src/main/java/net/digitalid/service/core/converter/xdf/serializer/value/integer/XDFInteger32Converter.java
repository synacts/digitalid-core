package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.Integer32Wrapper;
import net.digitalid.service.core.block.wrappers.value.integer.IntegerWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;

public class XDFInteger32Converter extends XDFConverter<Integer> {
    
    @Override
    protected @Nonnull Integer recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return Integer32Wrapper.decode(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        assert (object instanceof Integer) : "The object is an instance of the integer type.";

        final int value = (Integer) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, IntegerWrapper.XDF_TYPE);
        return Integer32Wrapper.encode(semanticType, value);
    }
    
}
