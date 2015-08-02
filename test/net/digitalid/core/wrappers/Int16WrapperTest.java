package net.digitalid.core.wrappers;

import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Int16Wrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Int16WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int16@test.digitalid.net").load(Int16Wrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final short value = (short) random.nextInt();
            Assert.assertEquals(value, new Int16Wrapper(new Int16Wrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
