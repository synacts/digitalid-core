package net.digitalid.service.core.wrappers;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link SelfcontainedWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class SelfcontainedWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@core.digitalid.net").load(StringWrapper.TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.map("int32@core.digitalid.net").load(Int32Wrapper.TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("selfcontained@core.digitalid.net").load(SelfcontainedWrapper.TYPE);
        
        final @Nonnull Block string = new StringWrapper(STRING, "This is a short string.").toBlock();
        final @Nonnull Block int32 = new Int32Wrapper(INT32, 123456789).toBlock();
        final @Nonnull Block[] blocks = new Block[] {string, int32};
        
        for (final @Nonnull Block block : blocks) {
            Assert.assertEquals(block, new SelfcontainedWrapper(new SelfcontainedWrapper(TYPE, block).toBlock()).getElement());
        }
    }
}
