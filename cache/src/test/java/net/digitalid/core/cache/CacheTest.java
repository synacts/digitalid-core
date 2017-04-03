package net.digitalid.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.tuples.Pair;

import net.digitalid.core.attribute.AttributeTypes;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class CacheTest extends CoreTest {
    
    @Test
    public void testTable() throws ExternalException {
        final @Nonnull InternalNonHostIdentity internalIdentity = AttributeTypes.NAME;
        CacheModule.invalidateCachedAttributeValues(internalIdentity);
        final @Nonnull Pair<@Nonnull Boolean, @Nullable AttributeValue> result = CacheModule.getCachedAttributeValue(null, internalIdentity, TimeBuilder.build(), AttributeTypes.NAME);
        CacheModule.setCachedAttributeValue(null, internalIdentity, TimeBuilder.build(), AttributeTypes.NAME, null, null);
    }
    
}
