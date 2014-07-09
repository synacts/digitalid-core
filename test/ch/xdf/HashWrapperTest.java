package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import java.math.BigInteger;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link HashWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class HashWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            BigInteger hash = new BigInteger(HashWrapper.LENGTH * 8, random);
            assertEquals(hash, new HashWrapper(new HashWrapper(hash).toBlock()).getValue());
        }
    }
    
}
