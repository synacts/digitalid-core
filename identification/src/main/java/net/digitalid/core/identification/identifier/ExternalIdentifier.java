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
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.Person;

/**
 * This interface models external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
 */
@Immutable
@GenerateConverter
public abstract class ExternalIdentifier extends RootClass implements NonHostIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return Identifier.isConforming(string) && string.contains(":");
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        final int index = string.indexOf(':');
        if (index < 1) { return false; }
        final @Nonnull String scheme = string.substring(0, index);
        switch (scheme) {
            case "email": return EmailIdentifier.isValid(string);
            case "mobile": return MobileIdentifier.isValid(string);
            default: return false;
        }
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a new external identifier with the given string.
     */
    @Pure
    @Recover
    public static @Nonnull ExternalIdentifier with(@Nonnull @Valid String string) {
        final @Nonnull String scheme = string.substring(0, string.indexOf(':'));
        switch (scheme) {
            case "email": return EmailIdentifier.with(string);
            case "mobile": return MobileIdentifier.with(string);
            default: throw CaseExceptionBuilder.withVariable("scheme").withValue(scheme).build();
        }
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull Person resolve() throws ExternalException;
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return getString();
    }
    
}
