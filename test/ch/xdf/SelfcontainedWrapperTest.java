package ch.xdf;

import ch.virtualid.exceptions.external.InvalidEncodingException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link SelfcontainedWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class SelfcontainedWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Block[] blocks = new Block[] {new StringWrapper("").toBlock(), new StringWrapper("String").toBlock()};
        for (Block block : blocks) {
            SelfcontainedWrapper selfcontainedWrapper = new SelfcontainedWrapper(new SelfcontainedWrapper("name@virtualid.ch", block).getBlock());
            assertEquals("name@virtualid.ch", selfcontainedWrapper.getIdentifier());
            assertEquals(block, selfcontainedWrapper.getElement());
        }
    }
}
