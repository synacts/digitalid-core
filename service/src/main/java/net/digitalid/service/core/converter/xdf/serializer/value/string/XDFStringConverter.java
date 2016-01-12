package net.digitalid.service.core.converter.xdf.serializer.value.string;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFStringConverter extends XDFConverter<String> {
    
    @Override
    protected @Nonnull String recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return StringWrapper.decodeNonNullable(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        assert object instanceof String: "The object is an instance of a string type.";
        
        final @Nonnull String value = (String) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, StringWrapper.XDF_TYPE);
        return StringWrapper.encodeNullable(semanticType, value);
    }
    
}

