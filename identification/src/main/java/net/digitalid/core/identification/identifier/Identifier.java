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
package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.Identity;

/**
 * This interface models identifiers.
 * 
 * @see InternalIdentifier
 * @see NonHostIdentifier
 */
@Immutable
@GenerateConverter
public interface Identifier extends RootInterface {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     * This method is called by the validity checkers of the subtypes to prevent infinite recursion.
     */
    @Pure
    public static boolean isConforming(@Nonnull String string) {
        return string.length() < 64;
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     * This method delegates the validation to the subtypes.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains(":") ? ExternalIdentifier.isValid(string) : InternalIdentifier.isValid(string);
    }
    
    /* -------------------------------------------------- String -------------------------------------------------- */
    
    /**
     * Returns the string of this identifier.
     * (The {@link MaxSize} annotation is needed so that an identifier can be used as a primary key in the database.)
     */
    @Pure
    public @Nonnull @Valid @MaxSize(64) String getString();
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a new identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull Identifier with(@Nonnull @Valid String string) {
        return string.contains(":") ? ExternalIdentifier.with(string) : InternalIdentifier.with(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    /**
     * Resolves this identifier to an identity.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    @NonCommitting
    public @Nonnull Identity resolve() throws ExternalException;
    
}
