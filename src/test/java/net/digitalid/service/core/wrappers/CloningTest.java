package net.digitalid.service.core.wrappers;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.attribute.AttributeTypes;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.setup.DatabaseSetup;
import net.digitalid.utility.database.configuration.Database;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Block}.
 */
public final class CloningTest extends DatabaseSetup {
    
    @Test
    public void testCloning() throws InvalidEncodingException, SQLException {
        try {
            Database.lock();
            final @Nonnull String[] strings = new String[] {"", "äöüéè", "This is a short string.", "This is a longer string in order to test different string lengths."};
            for (final @Nonnull String string : strings) {
                final @Nonnull Block block = new StringWrapper(AttributeTypes.NAME, string).toBlock();
                Assert.assertEquals(block, block.clone());
            }
        } finally {
            Database.unlock();
        }
    }
    
}
