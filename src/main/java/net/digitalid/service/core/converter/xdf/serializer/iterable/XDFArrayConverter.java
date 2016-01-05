package net.digitalid.service.core.converter.xdf.serializer.iterable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.utility.collections.annotations.elements.NullableElements;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.conversion.ConverterAnnotations;
import net.digitalid.utility.conversion.exceptions.ConverterNotFoundException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.StoringException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.exceptions.internal.InternalException;

/**
 * Converts an array into an XDF block and vice versa. The array is encoded as an XDF list.
 */
public class XDFArrayConverter extends XDFIterableConverter<Object[]> {
    
    @Override
    protected @Nonnull Object[] recoverNonNullable(@Nonnull Block block, @Nonnull Class<?> type, @Nonnull ConverterAnnotations metaData) throws InvalidEncodingException, RecoveryException, InternalException {
        @Nonnull @NullableElements ReadOnlyList<Block> listOfBlocks = ListWrapper.decodeNullableElements(block);
        @Nonnull Class<?> componentType = type.getComponentType();
        @Nonnull XDFConverter<?> componentConverter = XDF.FORMAT.getConverter(componentType);
        @Nonnull @NullableElements Object[] array = new Object[listOfBlocks.size()];
        int i = 0;
        for (@Nullable Block componentBlock : listOfBlocks) {
            array[i] = componentConverter.recoverNullable(componentBlock, componentType, metaData);
            i++;
        }
        return array;
    }
    
    @Override
    public @Nonnull Block convertNonNullable(@Nonnull Object object, @Nonnull Class<?> type, @Nonnull String fieldName, @Nullable String parentName, @Nonnull ConverterAnnotations metaData) throws ConverterNotFoundException, StoringException {
        assert object.getClass().isArray() : "The object is an array.";
        
        Object[] values = (Object[]) object;
        return varargsToXDFList(type, fieldName, parentName, metaData, values);
    }
    
}
