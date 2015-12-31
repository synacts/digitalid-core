package net.digitalid.service.core.converter.xdf.serializer.iterable;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.system.converter.exceptions.RestoringException;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

public class XDFArrayConverter extends XDFIterableConverter<Object[]> {
    
    @Override
    protected @Nonnull Object[] convertFromNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws RestoringException {
        @Nonnull @NullableElements ReadOnlyList<Block> listOfBlocks;
        try {
            listOfBlocks = ListWrapper.decodeNullableElements(block);
        } catch (InvalidEncodingException | InternalException e) {
            throw RestoringException.get(type, e);
        }
        Class<?> componentType = type.getComponentType();
        XDFConverter<?> componentConverter = getRestoringTypeConverter(componentType);
        Object[] array = new Object[listOfBlocks.size()];
        int i = 0;
        for (Block componentBlock : listOfBlocks) {
            array[i] = componentConverter.convertFromNullable(componentBlock, componentType, metaData);
            i++;
        }
        return array;
    }
    
    @Override
    public @Nonnull Block convertToNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull Map<Class<? extends Annotation>, Object> metaData) throws StoringException {
        assert object.getClass().isArray() : "The object is an array.";
        
        Object[] values = (Object[]) object;
        return varargsToXDFList(type, fieldName, parentName, metaData, values);
    }
    
}
