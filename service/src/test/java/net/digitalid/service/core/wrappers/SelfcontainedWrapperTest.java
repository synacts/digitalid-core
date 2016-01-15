package net.digitalid.service.core.wrappers;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.ExternalException;

import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.block.wrappers.value.integer.Integer32Wrapper;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.setup.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link SelfcontainedWrapper}.
 */
public final class SelfcontainedWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@core.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull SemanticType INT32 = SemanticType.map("int32@core.digitalid.net").load(Integer32Wrapper.XDF_TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("selfcontained@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
        
        final @Nonnull Block string = StringWrapper.encodeNonNullable(STRING, "This is a short string.");
        final @Nonnull Block int32 = Integer32Wrapper.encode(INT32, 123456789);
        final @Nonnull Block[] blocks = new Block[] {string, int32};
        
        for (final @Nonnull Block block : blocks) {
            Assert.assertEquals(block, SelfcontainedWrapper.decodeNonNullable(SelfcontainedWrapper.encodeNonNullable(TYPE, block)));
        }
    }
}
