package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.logging.exceptions.ExternalException;

import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.value.integer.Integer32Wrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link SelfcontainedWrapper}.
 */
public final class SelfcontainedWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws ExternalException {
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
