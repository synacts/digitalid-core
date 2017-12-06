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
package net.digitalid.core.resolution;

import javax.annotation.Nonnull;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class IdentifierResolverImplementationTest extends CoreTest {

    @Test
    public void testMapping() {
        final @Nonnull @NonLoaded SemanticType mapping1 = SemanticType.map("type@test.digitalid.net");
        final @Nonnull @NonLoaded SemanticType mapping2 = SemanticType.map("type@test.digitalid.net");
        assertThat(mapping1.getKey()).isEqualTo(mapping2.getKey());
    }

}
