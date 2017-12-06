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
package net.digitalid.core.pack;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.time.Time;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class PackTest extends CoreTest {
    
    public static final @Nonnull @Loaded SemanticType NAME = SemanticType.map("name@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.STRING).withCategories(Category.INTERNAL_NON_HOST_IDENTITIES).withCachingPeriod(Time.MONTH).build());
    
    @Test
    public void testEquals() {
        final @Nonnull Pack pack1 = Pack.pack(StringConverter.INSTANCE, "Test", NAME);
        final @Nonnull Pack pack2 = Pack.pack(StringConverter.INSTANCE, "Test", NAME);
        assertThat(pack1).isEqualTo(pack2);
    }
    
}
