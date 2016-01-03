package net.digitalid.service.core.converter.xdf.serializer.value.binary;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.binary.BinaryWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFBinaryConverter extends XDFConverter<Byte[]> {
    
    @Override
    protected @Nonnull Byte[] convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        try {
            byte[] bytes = BinaryWrapper.decodeNonNullable(block);
            @Nonnull Byte[] boxedBytes = new Byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                boxedBytes[i] = bytes[i];
            }
            return boxedBytes;
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
    }
    
    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert (object instanceof Byte[] || object instanceof byte[]) : "The object is an instance of a byte array type.";

        byte[] value;
        if (object instanceof Byte[]) {
            @Nonnull Byte[] byteArray = (Byte[]) object;
            value = new byte[byteArray.length];
            int i = 0;
            for (Byte b : byteArray) {
                value[i] = b;
                i++;
            }
        } else {
            value = (byte[]) object;
        }
        
        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, BinaryWrapper.XDF_TYPE);
        return BinaryWrapper.encodeNullable(semanticType, value);
    }
    
}
