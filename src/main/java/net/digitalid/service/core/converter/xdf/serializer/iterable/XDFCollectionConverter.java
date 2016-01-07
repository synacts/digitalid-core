package net.digitalid.service.core.converter.xdf.serializer.iterable;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableHashSet;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.readonly.ReadOnlySet;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.annotations.GenericTypes;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.conversion.exceptions.TypeUnknownException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * Converts a convertible object with a single iterable field into an XDF list block.
 */
public class XDFCollectionConverter<T extends Collection<T>> extends XDFIterableConverter<T> {
    
    private @Nonnull Class<?> getInstantiableClassForType(Class<?> type) throws TypeUnknownException {
        if (ReadOnlyList.class.isAssignableFrom(type)) {
            return FreezableArrayList.class;            
        } else if (List.class.isAssignableFrom(type)) {
            return FreezableArrayList.class;
        } else if (ReadOnlySet.class.isAssignableFrom(type)) {
            return FreezableHashSet.class;
        } else if (Set.class.isAssignableFrom(type)) {
            return FreezableHashSet.class;
        } else {
            throw TypeUnknownException.get(type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InternalException, InvalidEncodingException, RecoveryException {
        @Nonnull @NullableElements ReadOnlyList<Block> listOfBlocks = ListWrapper.decodeNullableElements(block);
        
        @Nullable GenericTypes genericTypes = (GenericTypes) metaData.get(GenericTypes.class);
        if (genericTypes == null || genericTypes.value()[0] == null) {
            throw RecoveryException.get(type, "The type of its elements is unknown.");
        }
        if (genericTypes.value().length > 1) {
            throw RecoveryException.get(type, "Multiple types where defines for this list, but only one expected.");
        }
        @Nonnull Class<?> genericType = genericTypes.value()[0];

        @Nonnull XDFConverter<?> elementConverter = XDF.FORMAT.getConverter(genericType);

        @Nonnull @NullableElements Object[] elementValues = new Object[listOfBlocks.size()];
        int i = 0;
        for (@Nullable Block blockElement : listOfBlocks) {
            elementValues[i] = elementConverter.recoverNullable(blockElement, genericType, null);
            i++;
        }

        @Nonnull Class<?> collectionType;
        if (Modifier.isAbstract(type.getModifiers())) {
            try {
                collectionType = getInstantiableClassForType(type);
            } catch (TypeUnknownException e) {
                throw RecoveryException.get(type, "Cannot find an instantiable class.");
            }
        } else {
            collectionType = type;
        }
        @Nonnull Object collection;
        try {
            collection = collectionType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw RecoveryException.get(type, "Failed to instantiate an instance of type '" + type + "'", e);
        }
        Collections.addAll((Collection<Object>) collection, elementValues);
        return (T) collection;
    }

    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws StoringException, InternalException {
        assert object instanceof Collection: "The field of the convertible '" + object + "' is an iterable";
        
        @Nonnull Collection<?> collection = (Collection<?>) object;
        
        return varargsToXDFList(type, fieldName, parentName, metaData, collection);
    }
    
}
