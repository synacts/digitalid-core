package net.digitalid.core.wrappers;

import java.math.BigInteger;
import java.util.Random;
import javax.annotation.Nonnull;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.setup.DatabaseSetup;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link HashWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class HashWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.map("hash@test.digitalid.net").load(HashWrapper.TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final @Nonnull BigInteger hash = new BigInteger(HashWrapper.LENGTH * 8, random);
            Assert.assertEquals(hash, new HashWrapper(new HashWrapper(TYPE, hash).toBlock()).getValue());
        }
    }
    
}
