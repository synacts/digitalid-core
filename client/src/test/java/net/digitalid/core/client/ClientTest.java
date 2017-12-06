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
package net.digitalid.core.client;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.ExternalException;

import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class ClientTest extends CoreTest {
    
    @Test
    public void testClientCreation() throws ExternalException {
        final @Nonnull Client client = ClientBuilder.withIdentifier("net.digitalid.test").withDisplayName("Test Client").withPreferredPermissions(ReadOnlyAgentPermissions.GENERAL_WRITE).build();
        assertThat(client.getDisplayName()).isEqualTo("Test Client");
        assertThat(client.getIdentifier()).isEqualTo("net.digitalid.test");
        assertThat(client.getName()).isEqualTo("net_digitalid_test");
    }
    
}
