package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.IntvarWrapper;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link IntvarWrapper}.
 */
public final class IntvarWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("intvar@test.digitalid.net").load(IntvarWrapper.TYPE);
        long value = 0;
        while (Long.numberOfLeadingZeros(value) >= 2) {
            Assert.assertEquals(value, new IntvarWrapper(new IntvarWrapper(TYPE, value).toBlock()).getValue());
            value = (value + 1) * 3;
        }
    }
    
}
