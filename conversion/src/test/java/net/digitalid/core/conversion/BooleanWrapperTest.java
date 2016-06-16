package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.server.DatabaseSetup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 */
public final class BooleanWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("boolean@test.digitalid.net").load(BooleanWrapper.XDF_TYPE);
        final boolean[] values = new boolean[] {true, false};
        for (final boolean value : values) {
            Assert.assertEquals(value, BooleanWrapper.decode(BooleanWrapper.encode(TYPE, value)));
        }
    }
    
}
