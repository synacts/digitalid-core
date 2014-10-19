package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link CompressionWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class CompressionWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType STRING = SemanticType.create("string@syntacts.com").load(StringWrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("compression@syntacts.com").load(CompressionWrapper.TYPE, STRING);
        final @Nonnull Block[] blocks = new Block[] {new StringWrapper(STRING, "").toBlock(), new StringWrapper(STRING, "This is a short string.").toBlock(), new StringWrapper(STRING, "This is a longer string in order to test different block lengths.").toBlock()};
        final @Nonnull byte[] algorithms = new byte[] {CompressionWrapper.NONE, CompressionWrapper.ZLIB};
        for (final @Nonnull Block block : blocks) {
            for (final byte algorithm : algorithms) {
//                System.out.println("Algorithm: " + algorithm + "; Uncompressed: " + block.getLength() + "; Compressed: " + new CompressionWrapper(TYPE, block, algorithm).toBlock().getLength());
                Assert.assertEquals(block, new CompressionWrapper(new CompressionWrapper(TYPE, block, algorithm).toBlock()).getElement());
            }
        }
    }
    
}
