package ch.xdf;

import ch.virtualid.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link ListWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ListWrapperTest extends DatabaseSetup {

    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.create("string@syntacts.com").load(StringWrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("list@syntacts.com").load(ListWrapper.TYPE, STRING);
        final @Nonnull Block block1 = new StringWrapper(STRING, "This is a short first block.").toBlock();
        final @Nonnull Block block2 = new StringWrapper(STRING, "This is a longer second block in order to test different block lengths.").toBlock();
        final @Nonnull Block block3 = new StringWrapper(STRING, "This is an even longer third block in order to test the wrapping of more than two blocks.").toBlock();
        
        final @Nonnull FreezableList<ReadonlyList<Block>> listOfElements = new FreezableLinkedList<ReadonlyList<Block>>();
        {
            @Nonnull FreezableList<Block> elements = new FreezableLinkedList<Block>();
            listOfElements.add(elements.freeze()); 
            elements = elements.clone();
            elements.add(block1);
            listOfElements.add(elements.freeze());
            elements = elements.clone();
            elements.add(block2);
            listOfElements.add(elements.freeze());
            elements = elements.clone();
            elements.add(block3);
            listOfElements.add(elements.freeze());
            elements = elements.clone();
            elements.add(null);
            listOfElements.add(elements.freeze());
        }
        
        for (final @Nonnull ReadonlyList<Block> elements : listOfElements) {
            Assert.assertEquals(elements, new ListWrapper(new ListWrapper(TYPE, elements).toBlock()).getElements());
        }
    }
    
}
