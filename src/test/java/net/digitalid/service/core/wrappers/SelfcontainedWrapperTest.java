package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.Int32Wrapper;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link SelfcontainedWrapper}.
 */
public final class SelfcontainedWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws AbortException, PacketException, ExternalException, NetworkException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@core.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.map("int32@core.digitalid.net").load(Int32Wrapper.XDF_TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("selfcontained@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
        
        final @Nonnull Block string = new StringWrapper(STRING, "This is a short string.").toBlock();
        final @Nonnull Block int32 = new Int32Wrapper(INT32, 123456789).toBlock();
        final @Nonnull Block[] blocks = new Block[] {string, int32};
        
        for (final @Nonnull Block block : blocks) {
            Assert.assertEquals(block, new SelfcontainedWrapper(new SelfcontainedWrapper(TYPE, block).toBlock()).getElement());
        }
    }
}
