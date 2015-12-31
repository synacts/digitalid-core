package net.digitalid.service.core.converter.xdf.serializer.iterable;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.system.converter.annotations.GenericTypes;
import net.digitalid.utility.system.converter.exceptions.StoringException;

/**
 * The converter implements helper methods which can be used to convert iterable objects into an XDF block.
 * 
 * @param <T> The type of the object to which an XDF block is converted to or from.
 */
public abstract class XDFIterableConverter<T> extends XDFConverter<T> {

    /**
     * Converts an array or collections field into an XDF list.
     * 
     * @param type the type of the field.
     * @param fieldName the name of the field.
     * @param parentName the name of the parent.
     * @param metaData the metaData of the field.
     * @param values the values of the field.
     *               
     * @return an XDF list block with the encoded values.
     * 
     * @throws StoringException thrown if the type of the iterable elements is unknown.
     */
    protected @Nonnull Block varargsToXDFList(@Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData, @Nonnull Object... values) throws StoringException {
        
        FreezableArrayList<Block> elements = FreezableArrayList.get();

        GenericTypes genericTypes = (GenericTypes) metaData.get(GenericTypes.class);
        if (genericTypes == null || genericTypes.types()[0] == null) {
            throw StoringException.get(type, "The type of its elements is unknown.");
        }
        Class<?> genericType = genericTypes.types()[0];
        XDFConverter<?> converter = getStoringTypeConverter(genericType);
        for (Object iterableValue : values) {
            Block element = converter.convertToNullable(iterableValue, type, fieldName, parentName, null);
            elements.add(element);
        }

        SemanticType semanticType = generateSemanticType("list." + fieldName, parentName, ListWrapper.XDF_TYPE);
        return ListWrapper.encode(semanticType, elements);
    }
    
}
