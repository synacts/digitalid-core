package net.digitalid.service.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.value.integer.Integer32Wrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Integer32Wrapper}.
 */
public final class Int32WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int32@test.digitalid.net").load(Integer32Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final int value = random.nextInt();
            Assert.assertEquals(value, Integer32Wrapper.decode(Integer32Wrapper.encode(TYPE, value)));
        }
    }
    
}
