package ch.xdf;

import ch.virtualid.client.Client;
import ch.virtualid.credential.Credential;
import ch.virtualid.server.Host;
import ch.virtualid.server.Server;
import java.math.BigInteger;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit testing of the class {@link SignatureWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class SignatureWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws Exception {
        Server.start(new String[0]);
        String identifier = "test.virtualid.ch";
        Host host = new Host(identifier);
        Client client = new Client("Tester");
        Block[] blocks = new Block[] {Block.EMPTY, new StringWrapper("String").toBlock()};
        for (Block block : blocks) {
            testSignature(block, identifier, 0, null, null, null, null, false); // Unsigned.
            testSignature(block, identifier, 0, null, identifier, null, null, false); // Signed by a host.
            testSignature(block, identifier, 0, null, null, client.getSecret(), null, false); // Signed by a client.
            // TODO: testSignature(block, identifier, 0, null, null, null, new Credential[0], false); // Signed with credentials.
        }
    }
    
    /**
     * Tests the signing mechanism with the given parameters.
     */
    private void testSignature(Block element, String identifier, long auditTime, List<Block> auditTrail, String host, BigInteger client, Credential[] credentials, boolean lodged) throws Exception {
        SignatureWrapper signatureWrapper = new SignatureWrapper(new SignatureWrapper(element, identifier, auditTime, auditTrail, host, client, credentials, lodged).getBlock(), true);
        assertEquals(element, signatureWrapper.getElement());
        assertEquals(identifier, signatureWrapper.getIdentifier());
        assertEquals(auditTime, signatureWrapper.getAuditTime());
        assertEquals(auditTrail, signatureWrapper.getAuditTrail());
        assertEquals(host, signatureWrapper.getSigner());
        assertArrayEquals(credentials, signatureWrapper.getCredentials());
        assertEquals(lodged, signatureWrapper.isLodged());
    }
    
}
