package net.digitalid.service.core.wrappers;

import net.digitalid.service.core.block.Block;

import net.digitalid.service.core.block.wrappers.Int32Wrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link TupleWrapper}.
 */
public final class TupleWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.map("int32@test.digitalid.net").load(Int32Wrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("tuple@test.digitalid.net").load(TupleWrapper.TYPE, STRING, INT32);
        final @Nonnull Block string = new StringWrapper(STRING, "This is a short string.").toBlock();
        final @Nonnull Block int32 = new Int32Wrapper(INT32, 123456789).toBlock();
        
        final @Nonnull FreezableList<ReadOnlyArray<Block>> listOfElements = new FreezableLinkedList<>();
        listOfElements.add(new FreezableArray<>(string, int32).freeze());
        listOfElements.add(new FreezableArray<>(null, int32).freeze());
        listOfElements.add(new FreezableArray<>(string, null).freeze());
        listOfElements.add(new FreezableArray<Block>(null, null).freeze());
        listOfElements.add(new FreezableArray<>(string).freeze());
        
        for (final @Nonnull ReadOnlyArray<Block> elements : listOfElements) {
            if (elements.size() == 2) Assert.assertEquals(elements, new TupleWrapper(new TupleWrapper(TYPE, elements).toBlock()).getNullableElements());
            else Assert.assertEquals(elements.getNonNullable(0), new TupleWrapper(new TupleWrapper(TYPE, elements).toBlock()).getNonNullableElement(0));
        }
    }
}
