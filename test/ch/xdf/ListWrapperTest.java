package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link ListWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class ListWrapperTest {

    /**
     * Tests the encoding and decoding of blocks.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Block block1 = new StringWrapper("This is a short first block.").toBlock();
        Block block2 = new StringWrapper("This is a longer second block in order to test different block lengths.").toBlock();
        Block block3 = new StringWrapper("This is an even longer third block in order to test the wrapping of more than two blocks.").toBlock();
        
        List<List<Block>> listOfElements = new LinkedList<List<Block>>();
        listOfElements.add(Arrays.asList(new Block[] {}));
        listOfElements.add(Arrays.asList(new Block[] {block1}));
        listOfElements.add(Arrays.asList(new Block[] {block1, block2}));
        listOfElements.add(Arrays.asList(new Block[] {block1, block2, block3}));
        
        for (List<Block> elements : listOfElements) {
            assertEquals(elements, new ListWrapper(new ListWrapper(elements).toBlock()).getElements());
        }
    }
}
