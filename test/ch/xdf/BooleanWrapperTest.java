package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class BooleanWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("boolean@syntacts.com").load(BooleanWrapper.TYPE);
        final boolean[] values = new boolean[] {true, false};
        for (final boolean value : values) {
            Assert.assertEquals(value, new BooleanWrapper(new BooleanWrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
