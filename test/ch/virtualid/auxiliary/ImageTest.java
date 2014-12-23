package ch.virtualid.auxiliary;

import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the {@link Image} class.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ImageTest extends DatabaseSetup {
    
    @Test
    public void testEqualityOfBlocking() throws InvalidEncodingException {
        Assert.assertEquals(Image.CLIENT, new Image(Image.CLIENT.toBlock()));
    }
    
}
