package ch.xdf;

import ch.virtualid.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import java.math.BigInteger;
import java.util.Random;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link HashWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class HashWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("hash@syntacts.com").load(HashWrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final @Nonnull BigInteger hash = new BigInteger(HashWrapper.LENGTH * 8, random);
            Assert.assertEquals(hash, new HashWrapper(new HashWrapper(TYPE, hash).toBlock()).getValue());
        }
    }
    
}
