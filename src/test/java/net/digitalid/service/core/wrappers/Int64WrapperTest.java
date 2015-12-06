package net.digitalid.service.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Integer64Wrapper}.
 */
public final class Int64WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int64@test.digitalid.net").load(Integer64Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final long value = random.nextLong();
            Assert.assertEquals(value, Integer64Wrapper.decode(Integer64Wrapper.encode(TYPE, value)));
        }
    }
    
}
