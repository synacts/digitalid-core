package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link DataWrapperTest}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class DataWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        byte[][] datas = new byte[][] {"".getBytes(), "This is a short string.".getBytes(), "This is a longer string in order to test different string lengths.".getBytes()};
        for (byte[] data : datas) {
            assertArrayEquals(data, new DataWrapper(new DataWrapper(data).toBlock()).getData());
        }
    }
}
