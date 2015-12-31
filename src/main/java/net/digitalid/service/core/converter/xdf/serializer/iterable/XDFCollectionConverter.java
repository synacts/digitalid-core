package net.digitalid.service.core.converter.xdf.serializer.iterable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableHashSet;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.readonly.ReadOnlySet;
import net.digitalid.utility.system.converter.annotations.GenericTypes;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.system.converter.exceptions.TypeUnknownException;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

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
    protected @Nonnull T convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        @Nonnull @NullableElements ReadOnlyList<Block> listOfBlocks;
        try {
            listOfBlocks = ListWrapper.decodeNullableElements(block);
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
        
        GenericTypes genericTypes = (GenericTypes) metaData.get(GenericTypes.class);
        if (genericTypes == null || genericTypes.types()[0] == null) {
            throw RestoringException.get(type, "The type of its elements is unknown.");
        }
        Class<?> genericType = genericTypes.types()[0];

        XDFConverter elementConverter = getRestoringTypeConverter(genericType);

        Object[] elementValues = new Object[listOfBlocks.size()];
        int i = 0;
        for (Block blockElement : listOfBlocks) {
            elementValues[i] = elementConverter.convertFromNullable(blockElement, genericType, null);
            i++;
        }

        @Nonnull Class<?> collectionType;
        if (Modifier.isAbstract(type.getModifiers())) {
            try {
                collectionType = getInstantiableClassForType(type);
            } catch (TypeUnknownException e) {
                throw RestoringException.get(type, "Cannot find an instantiable class.");
            }
        } else {
            collectionType = type;
        }
        Object collection;
        try {
            collection = collectionType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw RestoringException.get(type, "Failed to instantiate an instance of type '" + type + "'", e);
        }
        Collections.addAll((Collection<Object>) collection, elementValues);
        return (T) collection;
    }

    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert object instanceof Collection: "The field of the convertible '" + object + "' is an iterable";
        
        Collection<?> collection = (Collection<?>) object;
        
        return varargsToXDFList(type, fieldName, parentName, metaData, collection);
    }
    
}
