package net.digitalid.service.core.converter.xdf.serializer.value;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.utility.system.converter.Convertible;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;

public class XDFObjectConverter extends XDFConverter<Convertible> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull Convertible convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        @Nullable Convertible convertible = XDFConverter.convertFrom(block, (Class<? extends Convertible>) type);
        if (convertible == null) {
            throw RestoringException.get(type, "Non-null block converted unexpectedly to null object.");
        }
        return convertible;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert object instanceof Convertible : "The object is an instance of Convertible.";
        
        final @Nonnull Convertible convertible = (Convertible) object;
        final @Nullable Block block = XDFConverter.convertTo(convertible, (Class<? extends Convertible>) type, fieldName + "." + parentName);
        if (block == null) {
            throw StoringException.get(type, "Non-null object converted unexpectedly to null block.");
        }
        return block;
    }
    
}
