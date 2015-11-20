package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link ListWrapper}.
 */
public final class ListWrapperTest extends DatabaseSetup {

    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("list@test.digitalid.net").load(ListWrapper.XDF_TYPE, STRING);
        final @Nonnull Block block1 = new StringWrapper(STRING, "This is a short first block.").toBlock();
        final @Nonnull Block block2 = new StringWrapper(STRING, "This is a longer second block in order to test different block lengths.").toBlock();
        final @Nonnull Block block3 = new StringWrapper(STRING, "This is an even longer third block in order to test the wrapping of more than two blocks.").toBlock();
        
        final @Nonnull FreezableList<ReadOnlyList<Block>> listOfElements = new FreezableLinkedList<>();
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
        
        for (final @Nonnull ReadOnlyList<Block> elements : listOfElements) {
            Assert.assertEquals(elements, new ListWrapper(new ListWrapper(TYPE, elements).toBlock()).getElements());
        }
    }
    
}
