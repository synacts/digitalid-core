package net.digitalid.service.core.converter.xdf.serializer.iterable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableHashMap;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.system.converter.annotations.GenericTypesMap;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

public class XDFMapConverter<T extends Map<?, ?>> extends XDFConverter<T> {
    
    private @Nonnull Class<?> getKeyType(@Nullable GenericTypesMap genericTypesMap) {
        if (genericTypesMap != null) {
            return genericTypesMap.keyType();
        } else {
            return Object.class;
        }
    }
    
    private @Nonnull Class<?> getValueType(@Nullable GenericTypesMap genericTypesMap) {
        if (genericTypesMap != null) {
            return genericTypesMap.valueType();
        } else {
            return Object.class;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Object,Object> getMapInstance(Class<?> type, int initialSize) throws RestoringException {
        Map<Object, Object> map;
        if (!Modifier.isAbstract(type.getModifiers())) {
            try {
                map = (Map) type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw RestoringException.get(type, e);
            }
        } else {
            map = FreezableHashMap.get(initialSize);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @Nonnull T convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        try {
            GenericTypesMap genericTypesMap = (GenericTypesMap) metaData.get(GenericTypesMap.class);
            
            @Nonnull Class<?> keyType = getKeyType(genericTypesMap);
            @Nonnull Class<?> valueType = getValueType(genericTypesMap);

            @NonNullableElements ReadOnlyList<Block> listElements = ListWrapper.decodeNonNullableElements(block);
            Map<Object, Object> map = getMapInstance(type, listElements.size());
           
            for (@Nonnull Block listElement : listElements) {
                @Nonnull TupleWrapper tupleWrapper;
                try {
                    tupleWrapper = TupleWrapper.decode(listElement);
                } catch (InvalidEncodingException | InternalException e) {
                    throw RestoringException.get(type, e);
                }
                @Nonnull @NullableElements ReadOnlyArray<Block> elements = tupleWrapper.getNonNullableElements();
                
                assert elements.size() == 2 : "The element of a map is a tuple of degree 2.";
                assert elements.getNullable(0) != null : "The key of a map entry must not be null.";
                
                @Nonnull Block keyBlock = elements.getNonNullable(0);
                @Nullable Block valueBlock = elements.getNullable(1);

                @Nonnull XDFConverter<?> keyConverter = getRestoringTypeConverter(keyType);
                @Nonnull XDFConverter<?> valueConverter = getRestoringTypeConverter(valueType);

                Object keyObject = keyConverter.convertFromNullable(keyBlock, keyType, null);
                Object valueObject = valueConverter.convertFromNullable(valueBlock, valueType, null);

                map.put(keyObject, valueObject);
            }
            return (T) map;
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
    }

    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert Map.class.isAssignableFrom(object.getClass()) : "The object is a map.";
        
        Map<?, ?> map = (Map<?, ?>) object;

        FreezableList<Block> elements = FreezableArrayList.getWithCapacity(map.size());
        GenericTypesMap genericTypesMap = (GenericTypesMap) metaData.get(GenericTypesMap.class);
        Class<?> keyType = genericTypesMap.keyType();
        Class<?> valueType = genericTypesMap.valueType();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            XDFConverter<?> keyConverter = getStoringTypeConverter(keyType);
            XDFConverter<?> valueConverter = getStoringTypeConverter(valueType);

            SemanticType semanticType = generateSemanticType(fieldName, parentName, TupleWrapper.XDF_TYPE);
            Block pair = TupleWrapper.encode(semanticType, keyConverter.convertToNullable(key, keyType, keyType.getSimpleName(), fieldName + "." + parentName, null), valueConverter.convertToNullable(value, valueType, valueType.getSimpleName(), fieldName + "." + parentName, null));
            elements.add(pair);
        }
        
        SemanticType semanticType = generateSemanticType("map." + fieldName, parentName, ListWrapper.XDF_TYPE);
        return ListWrapper.encode(semanticType, elements);
    }
    
}
