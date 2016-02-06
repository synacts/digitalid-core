package net.digitalid.core.conversion.xdf.serializer.structure;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.NullableElements;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.generator.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.conversion.exceptions.StructureException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.InternalException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.conversion.xdf.XDFConverter;

import net.digitalid.core.identity.SemanticType;

/**
 * Converts an object holding multiple fields into an XDF tuple block.
 */
public class XDFTupleConverter<T extends Convertible> extends XDFConverter<T> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InvalidEncodingException, RecoveryException, InternalException {
        @Nonnull TupleWrapper tupleWrapper = TupleWrapper.decode(block);
        @Nonnull @NullableElements ReadOnlyArray<Block> elements = tupleWrapper.getNonNullableElements();
        final @Nonnull @NonNullableElements ReadOnlyList<Field> fields;
        try {
            fields = getConvertibleFields(type);
        } catch (StructureException e) {
            throw RecoveryException.get(type, e);
        }
        int i = 0;
        final @NullableElements Object[] values = new Object[fields.size()];
        for (@Nonnull Field field : fields) {
            @Nullable Object fieldValue = recoverNullable(elements.getNullable(i), field);
            values[i] = fieldValue;
        }
        @Nonnull Object restoredObject = recoverNonNullableObject(type, values);
        return (T) restoredObject;
    }

    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String typeName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws InternalException, StoringException {
        Require.that(object instanceof Convertible).orThrow("The object extends Convertible.");
        
        final @Nonnull Convertible convertible = (Convertible) object;
                
        final @Nonnull @NonNullableElements ReadOnlyList<Field> fields;
        try {
            fields = getConvertibleFields(type);
        } catch (StructureException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
        
        final @Nonnull @NullableElements FreezableArray<Block> elements = FreezableArray.get(fields.size());
        int i = 0;
        for (final @Nonnull Field field : fields) {
            final @Nullable Object fieldValue;
            try {
                field.setAccessible(true);
                fieldValue = field.get(convertible);
            } catch (IllegalAccessException e) {
                throw StoringException.get(type, "The field '" + field.getName() + "' cannot be accessed.");
            }

            final @Nonnull XDFConverter<?> converter = XDF.FORMAT.getConverter(field);
            final @Nullable Block serializedField = converter.convertNullable(fieldValue, field.getType(), field.getName(), typeName + (parentName == null ? "" : "." + parentName), getAnnotations(field));
            elements.set(i, serializedField);
            i++;
        }
        @Nonnull SemanticType semanticType = generateSemanticType(typeName, parentName, TupleWrapper.XDF_TYPE);
        return TupleWrapper.encode(semanticType, elements);
    }
    
}
