package net.digitalid.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int8Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Int8WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("int8@test.digitalid.net").load(Int8Wrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final byte value = (byte) random.nextInt();
            Assert.assertEquals(value, new Int8Wrapper(new Int8Wrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
