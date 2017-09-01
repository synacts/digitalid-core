package net.digitalid.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.tuples.Pair;

import net.digitalid.core.attribute.AttributeTypes;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.testing.CoreTest;

public class CacheTest extends CoreTest {
    
    @PureWithSideEffects
    // Tests are no longer possible in this artifact because some required tables are only created or indexed in higher artifacts: @org.junit.Test
    public void testTable() throws ExternalException {
        final @Nonnull InternalNonHostIdentity internalIdentity = AttributeTypes.NAME;
        CacheModule.invalidateCachedAttributeValues(internalIdentity);
        final @Nonnull Pair<@Nonnull Boolean, @Nullable AttributeValue> result = CacheModule.getCachedAttributeValue(null, internalIdentity, TimeBuilder.build(), AttributeTypes.NAME);
        CacheModule.setCachedAttributeValue(null, internalIdentity, TimeBuilder.build(), AttributeTypes.NAME, null, null);
    }
    
}
