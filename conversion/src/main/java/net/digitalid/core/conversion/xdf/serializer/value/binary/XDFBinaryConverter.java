package net.digitalid.core.conversion.xdf.serializer.value.binary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.value.binary.BinaryWrapper;
import net.digitalid.core.conversion.xdf.XDFConverter;
import net.digitalid.core.identity.SemanticType;

public class XDFBinaryConverter extends XDFConverter<Byte[]> {
    
    @Override
    protected @Nonnull Byte[] recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        final @Nonnull byte[] bytes = BinaryWrapper.decodeNonNullable(block);
        final @Nonnull Byte[] boxedBytes = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            boxedBytes[i] = bytes[i];
        }
        return boxedBytes;
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        Require.that((object instanceof Byte[] || object instanceof byte[])).orThrow("The object is an instance of a byte array type.");

        @Nonnull byte[] value;
        if (object instanceof Byte[]) {
            @Nonnull Byte[] byteArray = (Byte[]) object;
            value = new byte[byteArray.length];
            int i = 0;
            for (@Nonnull Byte b : byteArray) {
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
