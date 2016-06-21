package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link ListWrapper}.
 */
public final class ListWrapperTest extends DatabaseSetup {

    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("list@test.digitalid.net").load(ListWrapper.XDF_TYPE, STRING);
        final @Nonnull Block block1 = StringWrapper.encodeNonNullable(STRING, "This is a short first block.");
        final @Nonnull Block block2 = StringWrapper.encodeNonNullable(STRING, "This is a longer second block in order to test different block lengths.");
        final @Nonnull Block block3 = StringWrapper.encodeNonNullable(STRING, "This is an even longer third block in order to test the wrapping of more than two blocks.");
        
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
            Assert.assertEquals(elements, ListWrapper.encode(ListWrapper.decodeNullableElements(TYPE, elements)));
        }
    }
    
}
