package net.digitalid.core.asymmetrickey;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.testing.UtilityTest;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.group.Element;
import net.digitalid.core.parameters.Parameters;

import org.junit.BeforeClass;
import org.junit.Test;

public class KeyPairTest extends UtilityTest {
    
    /**
     * Sets the length of the cryptographic parameters.
     */
    @Impure
    @BeforeClass
    public static void initializeParameters() {
        Parameters.FACTOR.set(130);
        Parameters.RANDOM_EXPONENT.set(64);
        Parameters.CREDENTIAL_EXPONENT.set(64);
        Parameters.RANDOM_CREDENTIAL_EXPONENT.set(96);
        Parameters.BLINDING_EXPONENT.set(96);
        Parameters.RANDOM_BLINDING_EXPONENT.set(128);
        Parameters.VERIFIABLE_ENCRYPTION.set(128);
        Parameters.SYMMETRIC_KEY.set(128);
    }
    
    @Test
    public void testKeyPair() {
        @Nonnull Time time = TimeBuilder.build();
        final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
        final @Nonnull PrivateKey privateKey = keyPair.getPrivateKey();
        final @Nonnull PublicKey publicKey = keyPair.getPublicKey();
        Log.information("Key Pair Generation: " + time.ago().getValue() + " ms");
        
        assertThat(publicKey.verifySubgroupProof()).isTrue();
        
        for (int i = 0; i < 10; i++) {
            Log.information("Starting with another round:");
            final @Nonnull Element m = publicKey.getCompositeGroup().getRandomElement();
            time = TimeBuilder.build();
            final @Nonnull Element c = m.pow(publicKey.getE());
            Log.information("Encryption (only algorithm): " + time.ago().getValue() + " ms");
            time = TimeBuilder.build();
            assertThat(c.pow(privateKey.getD())).isEqualTo(m);
            Log.information("Decryption (slow algorithm): " + time.ago().getValue() + " ms");
            time = TimeBuilder.build();
            assertThat(privateKey.powD(c)).isEqualTo(m);
            Log.information("Decryption (fast algorithm): " + time.ago().getValue() + " ms");
        }
    }
    
}
