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
package net.digitalid.core.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.SymmetricKey;

/**
 * This class encrypts the wrapped object as a response for encoding and decrypts it for decoding.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ResponseEncryption<@Unspecifiable OBJECT> extends Encryption<OBJECT> {
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable HostIdentifier getRecipient() {
        return null;
    }
    
    /* -------------------------------------------------- Symmetric Key -------------------------------------------------- */
    
    /**
     * Returns the symmetric key that has been or will be used to encrypt the object.
     */
    @Pure
    @Provided
    public abstract @Nonnull SymmetricKey getSymmetricKey();
    
    /* -------------------------------------------------- Initialization Vector -------------------------------------------------- */
    
    /**
     * Returns the initialization vector of the symmetric encryption scheme (AES).
     */
    @Pure
    @Default("net.digitalid.core.symmetrickey.InitializationVectorBuilder.build()")
    public abstract @Nonnull InitializationVector getInitializationVector();
    
}
