package net.digitalid.core.cryptography;

import javax.annotation.Nonnull;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link KeyPair}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class KeyPairTest extends DatabaseSetup {
    
    @Test
    public void testKeyPair() {
        @Nonnull Time time = Time.getCurrent();
        final @Nonnull KeyPair keyPair = new KeyPair();
        final @Nonnull PrivateKey privateKey = keyPair.getPrivateKey();
        final @Nonnull PublicKey publicKey = keyPair.getPublicKey();
        System.out.println("\nKey Pair Generation: " + time.ago().getValue() + " ms\n");
        
        Assert.assertTrue(publicKey.verifySubgroupProof());
        
        for (int i = 0; i < 10; i++) {
            final @Nonnull Element m = publicKey.getCompositeGroup().getRandomElement();
            time = Time.getCurrent();
            final @Nonnull Element c = m.pow(publicKey.getE());
            System.out.println("Encryption (only algorithm): " + time.ago().getValue() + " ms");
            time = Time.getCurrent();
            Assert.assertEquals(c.pow(privateKey.getD()), m);
            System.out.println("Decryption (slow algorithm): " + time.ago().getValue() + " ms");
            time = Time.getCurrent();
            Assert.assertEquals(privateKey.powD(c), m);
            System.out.println("Decryption (fast algorithm): " + time.ago().getValue() + " ms\n");
        }
    }
    
}
