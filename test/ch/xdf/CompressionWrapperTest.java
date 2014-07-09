package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link CompressionWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class CompressionWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Block[] blocks = new Block[] {new StringWrapper("").toBlock(), new StringWrapper("This is a short string.").toBlock(), new StringWrapper("This is a longer string in order to test different string lengths.").toBlock()};
        byte[] algorithms = new byte[] {CompressionWrapper.NONE, CompressionWrapper.ZLIB};
        for (Block block : blocks) {
            for (byte algorithm : algorithms) {
//                System.out.println("Uncompressed: " + block.getLength() + "; Compressed: " + new CompressionWrapper(block, algorithm).getBlock().getLength());
                assertEquals(block, new CompressionWrapper(new CompressionWrapper(block, algorithm).toBlock()).getElement());
            }
        }
    }
}
