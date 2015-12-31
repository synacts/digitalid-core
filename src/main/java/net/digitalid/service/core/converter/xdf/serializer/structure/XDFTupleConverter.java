package net.digitalid.service.core.converter.xdf.serializer.structure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.system.converter.Convertible;
import net.digitalid.utility.system.converter.exceptions.FieldConverterException;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.system.converter.exceptions.StructureException;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

/**
 * Converts an object holding multiple fields into an XDF tuple block.
 */
public class XDFTupleConverter<T extends Convertible> extends XDFConverter<T> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        try {
            @Nonnull TupleWrapper tupleWrapper = TupleWrapper.decode(block);
            @Nonnull @NullableElements ReadOnlyArray<Block> elements = tupleWrapper.getNonNullableElements();
            ReadOnlyList<Field> fields;
            try {
                fields = getStorableFields(type);
            } catch (StructureException e) {
                throw RestoringException.get(type, e);
            }
            int i = 0;
            Object[] values = new Object[fields.size()];
            for (Field field : fields) {
                Object fieldValue = convertField(field, elements.getNullable(i));
                values[i] = fieldValue;
            }
            @Nonnull Object restoredObject = constructObjectNonNullable(type, values);
            return (T) restoredObject;
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
    }

    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String typeName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert object instanceof Convertible : "The object extends Convertible.";
        Convertible convertible = (Convertible) object;
                
        ReadOnlyList<Field> fields;
        try {
            fields = getStorableFields(type);
        } catch (StructureException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
        
        @Nonnull FreezableArray<Block> elements = FreezableArray.get(fields.size());
        int i = 0;
        for (Field field : fields) {
            @Nullable Object fieldValue;
            try {
                field.setAccessible(true);
                fieldValue = field.get(convertible);
            } catch (IllegalAccessException e) {
                throw StoringException.get(type, "The field '" + field.getName() + "' cannot be accessed.");
            }

            XDFConverter<?> converter;
            try {
                converter = getFieldConverter(field);
            } catch (FieldConverterException e) {
                throw StoringException.get(type, e.getMessage());
            }
            Block serializedField = converter.convertToNullable(fieldValue, field.getType(), field.getName(), typeName + "." + parentName, getFieldMetaData(field));
            elements.set(i, serializedField);
            i++;
        }
        SemanticType semanticType = generateSemanticType(typeName, parentName, TupleWrapper.XDF_TYPE);
        return TupleWrapper.encode(semanticType, elements);
    }
}
