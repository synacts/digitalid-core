package net.digitalid.core.conversion;

import java.math.BigInteger;
import java.util.Random;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.value.binary.Binary256Wrapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Binary256Wrapper}.
 */
public final class HashWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("hash@test.digitalid.net").load(Binary256Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final @Nonnull BigInteger hash = new BigInteger(Binary256Wrapper.LENGTH * 8, random);
            Assert.assertEquals(hash, new Binary256Wrapper(new Binary256Wrapper(TYPE, hash).toBlock()).getValue());
        }
    }
    
}