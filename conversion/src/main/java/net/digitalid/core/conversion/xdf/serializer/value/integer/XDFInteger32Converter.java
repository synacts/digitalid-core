package net.digitalid.core.conversion.xdf.serializer.value.integer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.value.integer.Integer32Wrapper;
import net.digitalid.core.conversion.wrappers.value.integer.IntegerWrapper;
import net.digitalid.core.conversion.xdf.XDFConverter;
import net.digitalid.core.identification.identity.SemanticType;

public class XDFInteger32Converter extends XDFConverter<Integer> {
    
    @Override
    protected @Nonnull Integer recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return Integer32Wrapper.decode(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        Require.that((object instanceof Integer)).orThrow("The object is an instance of the integer type.");

        final int value = (Integer) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, IntegerWrapper.XDF_TYPE);
        return Integer32Wrapper.encode(semanticType, value);
    }
    
}
