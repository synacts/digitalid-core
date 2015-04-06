package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link ListWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class ListWrapperTest extends DatabaseSetup {

    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.create("string@test.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("list@test.digitalid.net").load(ListWrapper.TYPE, STRING);
        final @Nonnull Block block1 = new StringWrapper(STRING, "This is a short first block.").toBlock();
        final @Nonnull Block block2 = new StringWrapper(STRING, "This is a longer second block in order to test different block lengths.").toBlock();
        final @Nonnull Block block3 = new StringWrapper(STRING, "This is an even longer third block in order to test the wrapping of more than two blocks.").toBlock();
        
        final @Nonnull FreezableList<ReadonlyList<Block>> listOfElements = new FreezableLinkedList<>();
        {
            @Nonnull FreezableList<Block> elements = new FreezableLinkedList<>();
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
