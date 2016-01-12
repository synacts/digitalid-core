package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.Integer08Wrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFInteger08Converter extends XDFConverter<Byte> {
    
    @Override
    protected @Nonnull Byte recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return Integer08Wrapper.decode(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        assert (object instanceof Byte) : "The object is an instance of the byte type.";

        final byte value = (Byte) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, Integer08Wrapper.XDF_TYPE);
        return Integer08Wrapper.encode(semanticType, value);
    }
    
}
