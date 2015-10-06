package net.digitalid.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link DataWrapperTest}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class DataWrapperTest extends DatabaseSetup {

    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("data@test.digitalid.net").load(BytesWrapper.TYPE);
        final @Nonnull byte[][] datas = new byte[][] {"".getBytes(), "This is a short string.".getBytes(), "This is a longer string in order to test different string lengths.".getBytes()};
        for (final @Nonnull byte[] data : datas) {
            Assert.assertArrayEquals(data, new BytesWrapper(new BytesWrapper(TYPE, data).toBlock()).getData());
        }
    }
    
}
