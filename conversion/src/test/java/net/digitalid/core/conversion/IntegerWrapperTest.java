package net.digitalid.core.conversion;

import java.math.BigInteger;
import java.util.Random;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.value.integer.IntegerWrapper;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link IntegerWrapper}.
 */
public final class IntegerWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("integer@test.digitalid.net").load(IntegerWrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10000; i = (i + 1) * 3) {
            @Nonnull BigInteger value = new BigInteger(i, random);
            if (i % 2 == 1) { value = value.negate(); }
            Assert.assertEquals(value, new IntegerWrapper(new IntegerWrapper(TYPE, value).toBlock()).getValue());
        }
    }
    
}
