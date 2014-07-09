package ch.xdf;

import ch.xdf.exceptions.InvalidEncodingException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link StringWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class StringWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws InvalidEncodingException {
        String[] strings = new String[] {"", "äöüéè", "This is a short string.", "This is a longer string in order to test different string lengths."};
        for (String string : strings) {
            assertEquals(string, new StringWrapper(new StringWrapper(string).toBlock()).getString());
        }
    }
    
}
