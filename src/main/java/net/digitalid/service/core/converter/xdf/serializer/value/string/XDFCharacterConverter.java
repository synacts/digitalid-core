package net.digitalid.service.core.converter.xdf.serializer.value.string;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.string.String01Wrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFCharacterConverter extends XDFConverter<Character> {
    
    @Override
    protected @Nonnull Character convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        try {
            return String01Wrapper.decode(block);
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
    }
    
    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert (object instanceof Character) : "The object is an instance of a character type.";

        final @Nonnull Character character = (Character) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, String01Wrapper.XDF_TYPE);
        return String01Wrapper.encode(semanticType, character);
    }
}
