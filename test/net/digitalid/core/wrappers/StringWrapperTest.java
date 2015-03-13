package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link StringWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class StringWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("string@test.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull String[] strings = new String[] {"", "äöüéè", "This is a short string.", "This is a longer string in order to test different string lengths."};
        for (final @Nonnull String string : strings) {
//            System.out.println(string);
            Assert.assertEquals(string, new StringWrapper(new StringWrapper(TYPE, string).toBlock()).getString());
        }
    }
    
}
