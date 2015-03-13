package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.attribute.AttributeType;
import net.digitalid.core.database.Database;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Block}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class CloningTest extends DatabaseSetup {
    
    @Test
    public void testCloning() throws InvalidEncodingException {
        try {
            Database.lock();
            final @Nonnull String[] strings = new String[] {"", "äöüéè", "This is a short string.", "This is a longer string in order to test different string lengths."};
            for (final @Nonnull String string : strings) {
                final @Nonnull Block block = new StringWrapper(AttributeType.NAME, string).toBlock();
                Assert.assertEquals(block, block.clone());
            }
        } finally {
            Database.unlock();
        }
    }
    
}
