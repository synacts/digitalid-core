package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link TupleWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class TupleWrapperTest {

    /**
     * Tests the encoding and decoding of blocks.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Block block1 = new StringWrapper("This is a short first block.").toBlock();
        Block block2 = new StringWrapper("This is a longer second block in order to test different block lengths.").toBlock();
        Block block3 = new StringWrapper("This is an even longer third block in order to test the wrapping of more than two blocks.").toBlock();
        
        Block[][] arrayOfElements = new Block[][] {
            new Block[] {},
            new Block[] {block1},
            new Block[] {block1, block2},
            new Block[] {block1, block2, block3}
        };
        
        for (Block[] elements : arrayOfElements) {
            assertArrayEquals(elements, new TupleWrapper(new TupleWrapper(elements).toBlock()).getElements());
        }
    }
}
