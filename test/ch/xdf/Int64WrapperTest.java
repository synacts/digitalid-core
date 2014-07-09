package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int64Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class Int64WrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            long value = random.nextLong();
            assertEquals(value, new Int64Wrapper(new Int64Wrapper(value).toBlock()).getValue());
        }
    }
    
}
