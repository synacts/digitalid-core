package net.digitalid.service.core.converter.xdf.serializer.structure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.EmptyWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.system.converter.Convertible;
import net.digitalid.utility.system.converter.exceptions.FieldConverterException;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.system.converter.exceptions.StructureException;

/**
 * Converts a convertible object with a single field into an XDF block.
 */
public class XDFSingleFieldConverter<T extends Convertible> extends XDFConverter<T> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        Field field;
        try {
            field = getStorableField(type);
        } catch (StructureException e) {
            throw RestoringException.get(type, e);
        }
        Object fieldValue = convertField(field, block);
        Object restoredObject = constructObjectNonNullable(type, fieldValue);
        return (T) restoredObject;
    }

    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String typeName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        try {
            Field field = getStorableFields(type).getNonNullable(0);
            
            Object fieldValue;
            try {
                field.setAccessible(true);
                fieldValue = field.get(object);
            } catch (IllegalAccessException e) {
                throw StoringException.get(type, e.getMessage(), e);
            }
            
            XDFConverter<?> converter = getFieldConverter(field);
            @Nullable Block block = converter.convertToNullable(fieldValue, field.getType(), field.getName(), typeName + "." + parentName, getFieldMetaData(field));

            if (block == null) {
                SemanticType semanticType = generateSemanticType(typeName, parentName, EmptyWrapper.XDF_TYPE);
                block = EmptyWrapper.encode(semanticType);
            }
            return block;
        } catch (StructureException | FieldConverterException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
    }
    
}
