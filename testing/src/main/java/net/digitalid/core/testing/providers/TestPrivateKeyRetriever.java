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
package net.digitalid.core.testing.providers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PrivateKey;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This class implements the {@link PrivateKeyRetriever} for unit tests.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class TestPrivateKeyRetriever implements PrivateKeyRetriever {
    
    @Pure
    protected abstract @Nonnull KeyPair getKeyPair();
    
    @Pure
    @Override
    public @Nonnull PrivateKey getPrivateKey(@Nonnull HostIdentifier host, @Nonnull Time time) {
        return getKeyPair().getPrivateKey();
    }
    
}
