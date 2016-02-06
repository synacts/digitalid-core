package net.digitalid.core.conversion.xdf.serializer.value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.InternalException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;

import net.digitalid.core.conversion.xdf.XDFConverter;

import net.digitalid.core.identity.SemanticType;

public class XDFBooleanConverter extends XDFConverter<Boolean> {

    @Override
    protected @Nonnull Boolean recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return BooleanWrapper.decode(block);
    }

    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        Require.that(object instanceof Boolean).orThrow("The object is of type boolean.");
        
        final @Nonnull Boolean value = (Boolean) object;
        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, BooleanWrapper.XDF_TYPE);
        return BooleanWrapper.encode(semanticType, value);
    }
}
