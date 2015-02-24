package net.digitalid.core.auxiliary;

import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the {@link Image} class.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class ImageTest extends DatabaseSetup {
    
    @Test
    public void testEqualityOfBlocking() throws InvalidEncodingException {
        Assert.assertEquals(Image.CLIENT, new Image(Image.CLIENT.toBlock()));
    }
    
}
