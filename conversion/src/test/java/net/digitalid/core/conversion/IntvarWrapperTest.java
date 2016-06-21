package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link VariableInteger}.
 */
public final class IntvarWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("intvar@test.digitalid.net").load(VariableInteger.XDF_TYPE);
        long value = 0;
        while (Long.numberOfLeadingZeros(value) >= 2) {
            Assert.assertEquals(value, new VariableInteger(new VariableInteger(TYPE, value).toBlock()).getValue());
            value = (value + 1) * 3;
        }
    }
    
}
