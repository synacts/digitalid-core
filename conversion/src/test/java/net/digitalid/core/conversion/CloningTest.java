package net.digitalid.core.conversion;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.database.core.Database;

import net.digitalid.core.attribute.AttributeTypes;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Block}.
 */
public final class CloningTest extends DatabaseSetup {
    
    @Test
    public void testCloning() throws InvalidEncodingException, InternalException, SQLException {
        try {
            Database.lock();
            final @Nonnull String[] strings = new String[] {"", "äöüéè", "This is a short string.", "This is a longer string in order to test different string lengths."};
            for (final @Nonnull String string : strings) {
                final @Nonnull Block block = StringWrapper.encodeNonNullable(AttributeTypes.NAME, string);
                Assert.assertEquals(block, block.clone());
            }
        } finally {
            Database.unlock();
        }
    }
    
}
