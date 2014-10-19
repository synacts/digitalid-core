package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link IntvarWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class IntvarWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("intvar@syntacts.com").load(IntvarWrapper.TYPE);
        long value = 0;
        while (Long.numberOfLeadingZeros(value) >= 2) {
            Assert.assertEquals(value, new IntvarWrapper(new IntvarWrapper(TYPE, value).toBlock()).getValue());
            value = (value + 1) * 3;
        }
    }
    
}
