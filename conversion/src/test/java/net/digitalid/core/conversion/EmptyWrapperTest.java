package net.digitalid.core.conversion;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;

import net.digitalid.core.conversion.wrappers.value.BooleanWrapper;
import net.digitalid.core.conversion.wrappers.value.EmptyWrapper;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.server.DatabaseSetup;

import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 */
public class EmptyWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException, InternalException {
        final @Nonnull SemanticType TYPE = SemanticType.map("empty@test.digitalid.net").load(EmptyWrapper.XDF_TYPE);
        new EmptyWrapper(new EmptyWrapper(TYPE).toBlock());
    }
    
}
