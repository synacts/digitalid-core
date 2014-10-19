package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyArray;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link TupleWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class TupleWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.create("string@syntacts.com").load(StringWrapper.TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.create("int32@syntacts.com").load(Int32Wrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("tuple@syntacts.com").load(TupleWrapper.TYPE, STRING, INT32);
        final @Nonnull Block string = new StringWrapper(STRING, "This is a short string.").toBlock();
        final @Nonnull Block int32 = new Int32Wrapper(INT32, 123456789).toBlock();
        
        final @Nonnull FreezableList<ReadonlyArray<Block>> listOfElements = new FreezableLinkedList<ReadonlyArray<Block>>();
        listOfElements.add(new FreezableArray<Block>(string, int32).freeze());
        listOfElements.add(new FreezableArray<Block>(null, int32).freeze());
        listOfElements.add(new FreezableArray<Block>(string, null).freeze());
        listOfElements.add(new FreezableArray<Block>(null, null).freeze());
        listOfElements.add(new FreezableArray<Block>(string).freeze());
        
        for (final @Nonnull ReadonlyArray<Block> elements : listOfElements) {
            if (elements.size() == 2) Assert.assertEquals(elements, new TupleWrapper(new TupleWrapper(TYPE, elements).toBlock()).getElements());
            else Assert.assertEquals(elements.getNotNull(0), new TupleWrapper(new TupleWrapper(TYPE, elements).toBlock()).getElementNotNull(0));
        }
    }
}
