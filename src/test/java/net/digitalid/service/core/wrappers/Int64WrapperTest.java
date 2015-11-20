package net.digitalid.service.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.Int64Wrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int64Wrapper}.
 */
public final class Int64WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int64@test.digitalid.net").load(Int64Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final long value = random.nextLong();
            Assert.assertEquals(value, new Int64Wrapper(new Int64Wrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
