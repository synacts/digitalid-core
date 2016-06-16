package net.digitalid.core.conversion.xdf.serializer.iterable;

import java.lang.reflect.Modifier;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableHashMap;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.annotations.GenericTypes;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.NullableElements;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.exceptions.InvalidNullElementException;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.conversion.xdf.XDFConverter;
import net.digitalid.core.identity.SemanticType;

public class XDFMapConverter<T extends Map<?, ?>> extends XDFConverter<T> {
    
    /**
     * Returns the type of the keys of a map.
     */
    private @Nonnull Class<?> getKeyType(@Nullable GenericTypes genericTypes) {
        if (genericTypes != null) {
            assert genericTypes.value().length == 2;
            return genericTypes.value()[0];
        } else {
            return Object.class;
        }
    }
    
    /**
     * Returns the type of the values of a map.
     */
    private @Nonnull Class<?> getValueType(@Nullable GenericTypes genericTypes) {
        if (genericTypes != null) {
            assert genericTypes.value().length == 2;
            return genericTypes.value()[1];
        } else {
            return Object.class;
        }
    }
    
    /**
     * Returns a map instance of a certain type. If the type is abstract, a freezable hash map is returned as a reasonable
     * default value. It might happen that the abstract type is not a super type of the Freezable hash map. In this case 
     * we throw an internal exception and give the responsibility back to the caller, who is supposed to prove an
     * instantiable type.
     */
    @SuppressWarnings("unchecked")
    private @Nonnull Map<Object,Object> getMapInstance(@Nonnull Class<?> type, int initialSize) throws RecoveryException, InternalException {
        final @Nonnull Map<Object, Object> map;
        if (!Modifier.isAbstract(type.getModifiers())) {
            try {
                map = (Map<Object, Object>) type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw RecoveryException.get(type, e);
            }
        } else {
            map = FreezableHashMap.get(initialSize);
            if (!type.isInstance(map)) {
                throw InternalException.get("Could not instantiate map of type '" + type + "'. Expected a non-abstract map.");
            }
        }
        return map;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws RecoveryException, InternalException, InvalidEncodingException {
        final @Nullable GenericTypes genericTypes = (GenericTypes) metaData.get(GenericTypes.class);
        
        final @Nonnull Class<?> keyType = getKeyType(genericTypes);
        final @Nonnull Class<?> valueType = getValueType(genericTypes);
        
        final @Nonnull @NonNullableElements @Frozen ReadOnlyList<Block> listElements = ListWrapper.decodeNonNullableElements(block);
        final @Nonnull Map<Object, Object> map = getMapInstance(type, listElements.size());
       
        for (@Nonnull Block listElement : listElements) {
            final @Nonnull TupleWrapper tupleWrapper = TupleWrapper.decode(listElement);
            final @Nonnull @NullableElements ReadOnlyArray<Block> elements = tupleWrapper.getNonNullableElements();
            
            Require.that(elements.size() == 2).orThrow("The element of a map is a tuple of degree 2.");
            Require.that(elements.getNullable(0) != null).orThrow("The key of a map entry must not be null.");
            
            // The key and value are nullable because there are map implementations that permit null values and keys
            // (e.g. the default implementation of this converter, FreezableHashMap). Therefore we allow it, but
            // the insertion might fail at runtime, if the actual map implementation does not allow it.
            final @Nullable Block keyBlock = elements.getNonNullable(0);
            final @Nullable Block valueBlock = elements.getNullable(1);
            
            final @Nonnull XDFConverter<?> keyConverter = XDF.FORMAT.getConverter(keyType);
            final @Nonnull XDFConverter<?> valueConverter = XDF.FORMAT.getConverter(valueType);
            
            final @Nullable Object keyObject = keyConverter.recoverNullable(keyBlock, keyType, null);
            final @Nullable Object valueObject = valueConverter.recoverNullable(valueBlock, valueType, null);
            
            try {
                map.put(keyObject, valueObject);
            } catch (NullPointerException e) {
                throw InvalidNullElementException.get("The map of type '" + type + "' does not permit null keys and/or values, but a null key or value was decoded from an XDF block.");
            }
        }
        return (T) map;
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws InternalException, StoringException {
        Require.that(Map.class.isAssignableFrom(object.getClass())).orThrow("The object is a map.");
        
        final @Nonnull Map<?, ?> map = (Map<?, ?>) object;
        
        final @Nonnull @NonNullableElements FreezableList<Block> elements = FreezableArrayList.getWithCapacity(map.size());
        final @Nonnull GenericTypes genericTypesMap = (GenericTypes) metaData.get(GenericTypes.class);
        final @Nonnull Class<?> keyType = getKeyType(genericTypesMap);
        final @Nonnull Class<?> valueType = getValueType(genericTypesMap);
        for (final @Nonnull Map.Entry<?, ?> entry : map.entrySet()) {
            final @Nullable Object key = entry.getKey();
            final @Nullable Object value = entry.getValue();
            final @Nonnull XDFConverter<?> keyConverter = XDF.FORMAT.getConverter(keyType);
            final @Nonnull XDFConverter<?> valueConverter = XDF.FORMAT.getConverter(valueType);
            
            final @Nonnull SemanticType semanticType = generateSemanticType(fieldName, parentName, TupleWrapper.XDF_TYPE);
            final @Nonnull String pairParentName = fieldName + (parentName == null ? "" : "." + parentName);
            final @Nonnull Block pair = TupleWrapper.encode(semanticType, keyConverter.convertNullable(key, keyType, keyType.getSimpleName(), pairParentName, null), valueConverter.convertNullable(value, valueType, valueType.getSimpleName(), pairParentName, null));
            elements.add(pair);
        }
        
        final @Nonnull SemanticType semanticType = generateSemanticType("map." + fieldName, parentName, ListWrapper.XDF_TYPE);
        return ListWrapper.encode(semanticType, elements);
    }
    
}
