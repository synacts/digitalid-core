package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link IntvarWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class IntvarWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        long value = 0;
        while (Long.numberOfLeadingZeros(value) >= 2) {
            assertEquals(value, new IntvarWrapper(new IntvarWrapper(value).toBlock()).getValue());
            value = (value + 1) * 3;
        }
    }
    
}
