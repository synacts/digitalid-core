package ch.xdf;

import ch.virtualid.DatabaseSetup;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link SelfcontainedWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class SelfcontainedWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull SemanticType STRING = SemanticType.create("string@syntacts.com").load(StringWrapper.TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.create("int32@syntacts.com").load(Int32Wrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.create("selfcontained@syntacts.com").load(SelfcontainedWrapper.TYPE);
        
        final @Nonnull Block string = new StringWrapper(STRING, "This is a short string.").toBlock();
        final @Nonnull Block int32 = new Int32Wrapper(INT32, 123456789).toBlock();
        final @Nonnull Block[] blocks = new Block[] {string, int32};
        
        for (final @Nonnull Block block : blocks) {
            Assert.assertEquals(block, new SelfcontainedWrapper(new SelfcontainedWrapper(TYPE, block).toBlock()).getElement());
        }
    }
}
