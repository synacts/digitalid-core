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
package net.digitalid.core.symmetrickey;

import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.crypto.spec.IvParameterSpec;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.size.Size;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * The random initialization vector ensures that the cipher-texts of the same content are different.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class InitializationVector extends IvParameterSpec implements RootInterface {
    
    /* -------------------------------------------------- Generator -------------------------------------------------- */
    
    /**
     * Returns an array of 16 random bytes.
     */
    @Pure
    public static @Capturable @Nonnull @Size(16) byte[] getRandomBytes() {
        final @Nonnull byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    protected InitializationVector(@Default(name = "RandomBytes", value = "InitializationVector.getRandomBytes()") @Nonnull @Size(16) byte[] bytes) {
        super(bytes);
    }
    
    /* -------------------------------------------------- Bytes -------------------------------------------------- */
    
    @Pure
    public @Capturable @Nonnull @Size(16) byte[] getBytes() {
        return super.getIV();
    }
    
}
