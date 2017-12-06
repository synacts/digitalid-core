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
package net.digitalid.core.subject.utility;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.subject.CoreSubject;

/**
 * This class is an alternative to {@link Void} to comply with non-nullable parameters and return values.
 * An example of this is the {@link CoreSubject#getKey()} method and you can use {@link None} if a {@link CoreSubject} has no key.
 */
@Stateless
@GenerateConverter
public final class None {
    
    /**
     * Creates a new none.
     */
    private None() {}
    
    /**
     * Stores the only instance of this class.
     */
    public static final @Nonnull None INSTANCE = new None();
    
    /**
     * Returns the only instance of this class.
     */
    @Pure
    @Recover
    public static @Nonnull None getInstance() {
        return INSTANCE;
    }
    
}
