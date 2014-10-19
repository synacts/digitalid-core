package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import java.util.Random;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int32Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Int32WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("int32@syntacts.com").load(Int32Wrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final int value = random.nextInt();
            Assert.assertEquals(value, new Int32Wrapper(new Int32Wrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
