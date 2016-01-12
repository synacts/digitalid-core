package net.digitalid.service.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.value.integer.Integer16Wrapper;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Integer16Wrapper}.
 */
public final class Int16WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int16@test.digitalid.net").load(Integer16Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final short value = (short) random.nextInt();
            Assert.assertEquals(value, Integer16Wrapper.decode(Integer16Wrapper.encode(TYPE, value)));
        }
    }
    
}
