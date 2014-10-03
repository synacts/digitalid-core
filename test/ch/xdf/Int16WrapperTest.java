package ch.xdf;

import ch.virtualid.exceptions.external.InvalidEncodingException;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int16Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class Int16WrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            short value = (short) random.nextInt();
            assertEquals(value, new Int16Wrapper(new Int16Wrapper(value).toBlock()).getValue());
        }
    }
}
