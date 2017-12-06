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
package net.digitalid.core.asymmetrickey;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.core.group.Element;
import net.digitalid.core.parameters.Parameters;

/**
 * Generates cryptographic hashes.
 */
@Utility
public abstract class HashGenerator {

    /**
     * Generates and returns a cryptographic hash using the SHA-256 hash algorithm on the values of the given elements.
     */
    @Pure
    public static @Nonnull BigInteger generateHash(@NonCaptured @Unmodified @Nonnull @NonNullableElements Element... elements) {
        final @Nonnull MessageDigest instance = Parameters.HASH_FUNCTION.get().produce();
        for (@Nonnull Element element : elements) {
            final @Nonnull byte[] bytes = element.getValue().toByteArray();
            instance.update(bytes); // TODO: Verify that this works!
            instance.update((byte) 0);
        }
        return new BigInteger(1, instance.digest());
    }
    
}
