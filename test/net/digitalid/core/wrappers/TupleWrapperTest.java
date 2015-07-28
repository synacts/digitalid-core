package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyArray;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link TupleWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class TupleWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.create("string@test.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.create("int32@test.digitalid.net").load(Int32Wrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("tuple@test.digitalid.net").load(TupleWrapper.TYPE, STRING, INT32);
        final @Nonnull Block string = new StringWrapper(STRING, "This is a short string.").toBlock();
        final @Nonnull Block int32 = new Int32Wrapper(INT32, 123456789).toBlock();
        
        final @Nonnull FreezableList<ReadOnlyArray<Block>> listOfElements = new FreezableLinkedList<>();
        listOfElements.add(new FreezableArray<>(string, int32).freeze());
        listOfElements.add(new FreezableArray<>(null, int32).freeze());
        listOfElements.add(new FreezableArray<>(string, null).freeze());
        listOfElements.add(new FreezableArray<Block>(null, null).freeze());
        listOfElements.add(new FreezableArray<>(string).freeze());
        
        for (final @Nonnull ReadOnlyArray<Block> elements : listOfElements) {
            if (elements.size() == 2) Assert.assertEquals(elements, new TupleWrapper(new TupleWrapper(TYPE, elements).toBlock()).getElements());
            else Assert.assertEquals(elements.getNonNullable(0), new TupleWrapper(new TupleWrapper(TYPE, elements).toBlock()).getElementNotNull(0));
        }
    }
}
