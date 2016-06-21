package net.digitalid.core.conversion;

import java.util.Random;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link Integer64Wrapper}.
 */
public final class Int64WrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("int64@test.digitalid.net").load(Integer64Wrapper.XDF_TYPE);
        final @Nonnull Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final long value = random.nextLong();
            Assert.assertEquals(value, Integer64Wrapper.decode(Integer64Wrapper.encode(TYPE, value)));
        }
    }
    
}
