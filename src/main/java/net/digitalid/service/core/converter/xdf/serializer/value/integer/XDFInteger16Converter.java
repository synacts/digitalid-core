package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.Integer16Wrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFInteger16Converter extends XDFConverter<Short> {
    
    @Override
    protected @Nonnull Short convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        try {
            return Integer16Wrapper.decode(block);
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
    }
    
    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert (object instanceof Short) : "The object is an instance of the byte type.";

        final short value = (Short) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, Integer16Wrapper.XDF_TYPE);
        return Integer16Wrapper.encode(semanticType, value);
    }
    
}
