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
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This class encrypts the wrapped object for encoding and decrypts it for decoding.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Encryption<@Unspecifiable OBJECT> {
    
    /* -------------------------------------------------- Encrypted Object -------------------------------------------------- */
    
    /**
     * Returns the object which has been or will be encrypted.
     */
    @Pure
    public abstract @Nonnull OBJECT getObject();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient for which the object has been or will be encrypted or null for responses.
     */
    @Pure
    public abstract @Nullable HostIdentifier getRecipient();
    
}
