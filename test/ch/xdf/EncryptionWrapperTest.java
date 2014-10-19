package ch.xdf;

import ch.virtualid.setup.ServerSetup;
import ch.virtualid.cryptography.SymmetricKey;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit testing of the class {@link EncryptionWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public final class EncryptionWrapperTest extends ServerSetup {

    @Test
    public void testWrapping() throws Exception {
        final @Nonnull SemanticType TYPE = SemanticType.create("boolean@syntacts.com").load(BooleanWrapper.TYPE);
        
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
