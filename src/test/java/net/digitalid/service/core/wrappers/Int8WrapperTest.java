package net.digitalid.service.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.value.integer.Integer08Wrapper;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Integer08Wrapper}.
 */
public final class Int8WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int8@test.digitalid.net").load(Integer08Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final byte value = (byte) random.nextInt();
            Assert.assertEquals(value, Integer08Wrapper.decode(Integer08Wrapper.encode(TYPE, value)));
        }
    }
    
}
