package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class BooleanWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        boolean[] values = new boolean[] {true, false};
        for (boolean value : values) {
            assertEquals(value, new BooleanWrapper(new BooleanWrapper(value).toBlock()).getValue());
        }
    }
    
}
