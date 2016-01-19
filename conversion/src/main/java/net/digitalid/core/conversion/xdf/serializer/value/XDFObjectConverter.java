package net.digitalid.core.conversion.xdf.serializer.value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.conversion.xdf.XDFConverter;

public class XDFObjectConverter<T extends Convertible> extends XDFConverter<T> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InvalidEncodingException, InternalException, RecoveryException {
        @Nonnull Convertible convertible = XDF.recoverNonNullable(block, (Class<T>) type);
        return (T) convertible;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException, InternalException {
        assert object instanceof Convertible : "The object is an instance of Convertible.";
        
        final @Nonnull Convertible convertible = (Convertible) object;
        return XDF.convertNonNullable(convertible, (Class<? extends Convertible>) type, fieldName + (parentName == null ? "" : "." + parentName));
    }
    
}
