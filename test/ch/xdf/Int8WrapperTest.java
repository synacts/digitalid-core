package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import java.util.Random;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int8Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Int8WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("int8@syntacts.com").load(Int8Wrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final byte value = (byte) random.nextInt();
            Assert.assertEquals(value, new Int8Wrapper(new Int8Wrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
