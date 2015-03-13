package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class BooleanWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("boolean@test.digitalid.net").load(BooleanWrapper.TYPE);
        final boolean[] values = new boolean[] {true, false};
        for (final boolean value : values) {
            Assert.assertEquals(value, new BooleanWrapper(new BooleanWrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
