/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
