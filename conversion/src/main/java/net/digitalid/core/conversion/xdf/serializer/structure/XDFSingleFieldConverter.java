package net.digitalid.core.conversion.xdf.serializer.structure;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.generator.conversion.Convertible;
import net.digitalid.utility.conversion.exceptions.ConverterNotFoundException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.conversion.exceptions.StructureException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.InternalException;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.value.EmptyWrapper;

import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.conversion.xdf.XDFConverter;

import net.digitalid.core.identity.SemanticType;

/**
 * Converts a convertible object with a single field into an XDF block.
 */
public class XDFSingleFieldConverter<T extends Convertible> extends XDFConverter<T> {
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InvalidEncodingException, RecoveryException, InternalException {
        final @Nonnull Field field;
        try {
            field = getConvertibleField(type);
        } catch (StructureException e) {
            throw RecoveryException.get(type, e);
        }
        final @Nullable Object fieldValue = recoverNullable(block, field);
        final @Nonnull Object restoredObject = recoverNonNullableObject(type, fieldValue);
        return (T) restoredObject;
    }

    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String typeName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws ConverterNotFoundException, StoringException {
        final @Nonnull Field field;
        try {
            field = getConvertibleFields(type).getNonNullable(0);
        } catch (StructureException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
            
        final @Nonnull Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(object);
        } catch (IllegalAccessException e) {
            throw StoringException.get(type, e.getMessage(), e);
        }
        
        final @Nonnull XDFConverter<?> converter = XDF.FORMAT.getConverter(field);
        @Nullable Block block = converter.convertNullable(fieldValue, field.getType(), field.getName(), typeName + "." + parentName, getAnnotations(field));

        if (block == null) {
            final @Nonnull SemanticType semanticType = generateSemanticType(typeName, parentName, EmptyWrapper.XDF_TYPE);
            block = EmptyWrapper.encode(semanticType);
        }
        return block;
    }
    
}
