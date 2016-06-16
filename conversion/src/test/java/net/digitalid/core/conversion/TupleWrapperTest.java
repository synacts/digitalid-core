package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
import net.digitalid.core.conversion.wrappers.value.integer.Integer32Wrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link TupleWrapper}.
 */
public final class TupleWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.map("int32@test.digitalid.net").load(Integer32Wrapper.XDF_TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("tuple@test.digitalid.net").load(TupleWrapper.XDF_TYPE, STRING, INT32);
        final @Nonnull Block string = StringWrapper.encodeNonNullable(STRING, "This is a short string.");
        final @Nonnull Block int32 = Integer32Wrapper.encode(INT32, 123456789);
        
        final @Nonnull FreezableList<ReadOnlyArray<Block>> listOfElements = new FreezableLinkedList<>();
        listOfElements.add(FreezableArray.getNonNullable(string, int32).freeze());
        listOfElements.add(FreezableArray.getNonNullable(null, int32).freeze());
        listOfElements.add(FreezableArray.getNonNullable(string, null).freeze());
        listOfElements.add(FreezableArray.<Block>getNonNullable(null, null).freeze());
        listOfElements.add(FreezableArray.getNonNullable(string).freeze());
        
        for (final @Nonnull ReadOnlyArray<Block> elements : listOfElements) {
            if (elements.size() == 2) { Assert.assertEquals(elements, TupleWrapper.decode(TupleWrapper.encode(TYPE, elements)).getNullableElements()); }
            else { Assert.assertEquals(elements.getNonNullable(0), TupleWrapper.decode(TupleWrapper.encode(TYPE, elements)).getNonNullableElement(0)); }
        }
    }
}
