package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 */
public final class BooleanWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("boolean@test.digitalid.net").load(BooleanWrapper.TYPE);
        final boolean[] values = new boolean[] {true, false};
        for (final boolean value : values) {
            Assert.assertEquals(value, new BooleanWrapper(new BooleanWrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
