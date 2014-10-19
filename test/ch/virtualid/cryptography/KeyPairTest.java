package ch.virtualid.cryptography;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.setup.DatabaseSetup;
import javax.annotation.Nonnull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit testing of the class {@link KeyPair}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class KeyPairTest extends DatabaseSetup {
    
    @Test
    public void testKeyPair() {
        @Nonnull Time time = new Time();
        final @Nonnull KeyPair keyPair = new KeyPair();
        final @Nonnull PrivateKey privateKey = keyPair.getPrivateKey();
        final @Nonnull PublicKey publicKey = keyPair.getPublicKey();
        System.out.println("\nKey Pair Generation: " + time.ago().getValue() + " ms\n");
        
        assertTrue(publicKey.verifySubgroupProof());
        
        for (int i = 0; i < 10; i++) {
            final @Nonnull Element m = publicKey.getCompositeGroup().getRandomElement();
            time = new Time();
            final @Nonnull Element c = m.pow(publicKey.getE());
            System.out.println("Encryption (only algorithm): " + time.ago().getValue() + " ms");
            time = new Time();
            assertEquals(c.pow(privateKey.getD()), m);
            System.out.println("Decryption (slow algorithm): " + time.ago().getValue() + " ms");
            time = new Time();
            assertEquals(privateKey.powD(c), m);
            System.out.println("Decryption (fast algorithm): " + time.ago().getValue() + " ms\n");
        }
    }
    
}
