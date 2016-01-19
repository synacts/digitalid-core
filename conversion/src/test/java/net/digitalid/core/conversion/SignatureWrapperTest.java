package net.digitalid.core.conversion;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.Nonnull;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;

import net.digitalid.core.credential.Credential;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.server.ServerSetup;

import org.junit.Test;

/**
 * Unit testing of the class {@link SignatureWrapper}.
 */
public final class SignatureWrapperTest extends ServerSetup {
    
    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws Exception {
        final @Nonnull SemanticType TYPE = SemanticType.map("boolean@test.digitalid.net").load(BooleanWrapper.XDF_TYPE);
        
//        Block[] blocks = new Block[] {Block.EMPTY, StringWrapper.encodeNonNullable("String")};
//        for (final @Nonnull Block block : blocks) {
//            testSignature(block, identifier, 0, null, null, null, null, false); // Unsigned.
//            testSignature(block, identifier, 0, null, identifier, null, null, false); // Signed by a host.
//            testSignature(block, identifier, 0, null, null, client.getSecret(), null, false); // Signed by a client.
//            // TODO: testSignature(block, identifier, 0, null, null, null, new Credential[0], false); // Signed with credentials.
//        }
    }
    
    /**
     * Tests the signing mechanism with the given parameters.
     */
    private void testSignature(Block element, String identifier, long auditTime, List<Block> auditTrail, String host, BigInteger client, Credential[] credentials, boolean lodged) throws Exception {
//        SignatureWrapper signatureWrapper = new SignatureWrapper(new SignatureWrapper(element, identifier, auditTime, auditTrail, host, client, credentials, lodged).getBlock(), true);
//        Assert.assertEquals(element, signatureWrapper.getElement());
//        Assert.assertEquals(identifier, signatureWrapper.getIdentifier());
//        Assert.assertEquals(auditTime, signatureWrapper.getAuditTime());
//        Assert.assertEquals(auditTrail, signatureWrapper.getAuditTrail());
//        Assert.assertEquals(host, signatureWrapper.getSigner());
//        Assert.assertArrayEquals(credentials, signatureWrapper.getCredentials());
//        Assert.assertEquals(lodged, signatureWrapper.isLodged());
    }
    
}
