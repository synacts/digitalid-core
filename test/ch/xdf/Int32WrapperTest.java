package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int32Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class Int32WrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int value = random.nextInt();
            assertEquals(value, new Int32Wrapper(new Int32Wrapper(value).toBlock()).getValue());
        }
    }
}
