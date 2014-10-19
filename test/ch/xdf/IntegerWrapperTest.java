package ch.xdf;

import ch.virtualid.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import java.math.BigInteger;
import java.util.Random;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link IntegerWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class IntegerWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("integer@syntacts.com").load(IntegerWrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10000; i = (i + 1) * 3) {
            @Nonnull BigInteger value = new BigInteger(i, random);
            if (i % 2 == 1) value = value.negate();
            Assert.assertEquals(value, new IntegerWrapper(new IntegerWrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
