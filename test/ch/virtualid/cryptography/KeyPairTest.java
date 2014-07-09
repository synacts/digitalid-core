package ch.virtualid.cryptography;

import javax.annotation.Nonnull;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the key pair generation of Virtual ID.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
public final class KeyPairTest {
    
    /**
     * Tests the generation of cryptographic key pairs.
     */
    @Test
    public void testKeyPair() {
        @Nonnull KeyPair keyPair = new KeyPair();
        @Nonnull PrivateKey privateKey = keyPair.getPrivateKey();
        @Nonnull PublicKey publicKey = keyPair.getPublicKey();
        
        assertTrue(publicKey.verifySubgroupProof());
        
        @Nonnull Group compositeGroup = publicKey.getCompositeGroup();
        for (int i = 0; i < 20; i++) {
            @Nonnull Element m = compositeGroup.getRandomElement();
            @Nonnull Element c = m.pow(publicKey.getE());
            long start = System.currentTimeMillis();
            assertEquals(c.pow(privateKey.getD()), m);
            System.out.println("" + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            assertEquals(privateKey.powD(c), m);
            System.out.println("-> " + (System.currentTimeMillis() - start) + " ms");
        }
    }
}
