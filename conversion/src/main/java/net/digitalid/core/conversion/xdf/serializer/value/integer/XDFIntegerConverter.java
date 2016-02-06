package net.digitalid.core.conversion.xdf.serializer.value.integer;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.InternalException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.value.integer.IntegerWrapper;

import net.digitalid.core.conversion.xdf.XDFConverter;

import net.digitalid.core.identity.SemanticType;

public class XDFIntegerConverter extends XDFConverter<BigInteger> {
    
    @Override
    protected @Nonnull BigInteger recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException {
        return IntegerWrapper.decodeNonNullable(block);
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException {
        Require.that((object instanceof BigInteger)).orThrow("The object is an instance of the BigInteger type.");

        final @Nonnull BigInteger value = (BigInteger) object;

        final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, IntegerWrapper.XDF_TYPE);
        return IntegerWrapper.encodeNullable(semanticType, value);
    }
    
}
