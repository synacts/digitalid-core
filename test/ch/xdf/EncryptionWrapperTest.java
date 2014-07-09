package ch.xdf;

import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.server.Host;
import ch.virtualid.server.Server;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link EncryptionWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class EncryptionWrapperTest {

    /**
     * Tests the encoding and decoding of values.
     */
    @Test
    public void testWrapping() throws Exception {
        Server.start(new String[0]);
        String identifier = "test.virtualid.ch";
        Host host = new Host(identifier);
        Block[] blocks = new Block[] {Block.EMPTY, new StringWrapper("String").toBlock()};
        for (Block block : blocks) {
            SymmetricKey[] symmetricKeys = new SymmetricKey[] {null, new SymmetricKey()};
            for (SymmetricKey symmetricKey : symmetricKeys) {
                
                // From client to host:
                EncryptionWrapper encryptionWrapper = new EncryptionWrapper(new EncryptionWrapper(block, identifier, symmetricKey).getBlock());
                assertEquals(block, encryptionWrapper.getElement());
                assertEquals(identifier, encryptionWrapper.getRecipient());
                assertEquals(symmetricKey, encryptionWrapper.getSymmetricKey());
                
                // From host to client:
                encryptionWrapper = new EncryptionWrapper(new EncryptionWrapper(block, null, symmetricKey).toBlock(), symmetricKey);
                assertEquals(block, encryptionWrapper.getElement());
                assertEquals(null, encryptionWrapper.getRecipient());
                assertEquals(symmetricKey, encryptionWrapper.getSymmetricKey());
            }
        }
    }
}
