package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.CompressionWrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link CompressionWrapper}.
 */
public final class CompressionWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType STRING = SemanticType.map("string@test.digitalid.net").load(StringWrapper.XDF_TYPE);
        final @Nonnull SemanticType TYPE = SemanticType.map("compression@test.digitalid.net").load(CompressionWrapper.XDF_TYPE, STRING);
        final @Nonnull Block[] blocks = new Block[] {StringWrapper.encodeNonNullable(STRING, ""), StringWrapper.encodeNonNullable(STRING, "This is a short string."), StringWrapper.encodeNonNullable(STRING, "This is a longer string in order to test different block lengths.")};
        final @Nonnull byte[] algorithms = new byte[] {CompressionWrapper.NONE, CompressionWrapper.ZLIB};
        for (final @Nonnull Block block : blocks) {
            for (final byte algorithm : algorithms) {
//                System.out.println("Algorithm: " + algorithm + "; Uncompressed: " + block.getLength() + "; Compressed: " + new CompressionWrapper(TYPE, block, algorithm).toBlock().getLength());
                Assert.assertEquals(block, new CompressionWrapper(new CompressionWrapper(TYPE, block, algorithm).toBlock()).getNullableElement());
            }
        }
    }
    
}
