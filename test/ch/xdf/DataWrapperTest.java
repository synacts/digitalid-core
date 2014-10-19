package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link DataWrapperTest}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class DataWrapperTest extends DatabaseSetup {

    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("data@syntacts.com").load(DataWrapper.TYPE);
        final @Nonnull byte[][] datas = new byte[][] {"".getBytes(), "This is a short string.".getBytes(), "This is a longer string in order to test different string lengths.".getBytes()};
        for (final @Nonnull byte[] data : datas) {
            Assert.assertArrayEquals(data, new DataWrapper(new DataWrapper(TYPE, data).toBlock()).getData());
        }
    }
    
}
