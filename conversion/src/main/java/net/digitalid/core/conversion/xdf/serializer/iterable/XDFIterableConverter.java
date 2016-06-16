package net.digitalid.core.conversion.xdf.serializer.iterable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.annotations.GenericTypes;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.elements.NullableElements;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.conversion.xdf.XDFConverter;
import net.digitalid.core.identity.SemanticType;

/**
 * The converter implements helper methods which can be used to convert iterable objects into an XDF block.
 * 
 * @param <T> The type of the object to which an XDF block is converted to or from.
 */
public abstract class XDFIterableConverter<T> extends XDFConverter<T> {

    /**
     * Converts an array or collections field into an XDF list.
     */
    protected @Nonnull Block varargsToXDFList(@Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData, @Nullable @NullableElements Object... values) throws InternalException, StoringException {
        
        final @Nonnull @NullableElements FreezableArrayList<Block> elements = FreezableArrayList.get();

        final @Nullable GenericTypes genericTypes = (GenericTypes) metaData.get(GenericTypes.class);
        if (genericTypes == null || genericTypes.value().length == 0) {
            throw StoringException.get(type, "The type of its elements is unknown.");
        }
        if (genericTypes.value().length > 1) {
            throw StoringException.get(type, "Multiple types where defines for this list, but only one expected.");
        }
        final @Nonnull Class<?> genericType = genericTypes.value()[0];
        final @Nonnull XDFConverter<?> converter = XDF.FORMAT.getConverter(genericType);
        if (values == null) {
            elements.add(null);
        } else {
            for (@Nullable Object iterableValue : values) {
                final @Nullable Block element = converter.convertNullable(iterableValue, type, fieldName, parentName, null);
                elements.add(element);
            }
        }
        final @Nonnull SemanticType semanticType = generateSemanticType("list." + fieldName, parentName, ListWrapper.XDF_TYPE);
        return ListWrapper.encode(semanticType, elements);
    }
    
}
