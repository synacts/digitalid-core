package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link IntegerWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class IntegerWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Random random = new Random();
        for (int i = 0; i < 10000; i = (i + 1) * 3) {
            BigInteger value = new BigInteger(i, random);
            if (i % 2 == 1) value = value.negate();
            assertEquals(value, new IntegerWrapper(new IntegerWrapper(value).toBlock()).getValue());
        }
    }
    
}
