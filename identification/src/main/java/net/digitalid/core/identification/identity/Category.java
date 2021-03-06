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
package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.Empty;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

/**
 * This class enumerates the various categories of digital identities.
 */
@Immutable
@GenerateConverter
public enum Category {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * The category for a host.
     */
    HOST(0),
    
    /**
     * The category for a syntactic type.
     */
    SYNTACTIC_TYPE(1),
    
    /**
     * The category for a semantic type.
     */
    SEMANTIC_TYPE(2),
    
    /**
     * The category for a natural person.
     */
    NATURAL_PERSON(3),
    
    /**
     * The category for an artificial person.
     */
    ARTIFICIAL_PERSON(4),
    
    /**
     * The category for an email person.
     */
    EMAIL_PERSON(5),
    
    /**
     * The category for a mobile person.
     */
    MOBILE_PERSON(6);
    
    /* -------------------------------------------------- Lists -------------------------------------------------- */
    
    /**
     * Stores an empty list of categories that can be shared among semantic types.
     */
    public static final @Nonnull @Empty ImmutableList<Category> NONE = ImmutableList.withElements();
    
    /**
     * Stores an immutable list with only the host category.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> ONLY_HOST = ImmutableList.withElements(HOST);
    
    /**
     * Stores an immutable list with only the natural person category.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> ONLY_NATURAL_PERSON = ImmutableList.withElements(NATURAL_PERSON);
    
    /**
     * Stores an immutable list with the internal person categories.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> INTERNAL_PERSONS = ImmutableList.withElements(NATURAL_PERSON, ARTIFICIAL_PERSON);
    
    /**
     * Stores an immutable list with the external person categories.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> EXTERNAL_PERSONS = ImmutableList.withElements(EMAIL_PERSON, MOBILE_PERSON);
    
    /**
     * Stores an immutable list with the person categories.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> PERSONS = ImmutableList.withElements(NATURAL_PERSON, ARTIFICIAL_PERSON, EMAIL_PERSON, MOBILE_PERSON);
    
    /**
     * Stores an immutable list with the type categories.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> TYPES = ImmutableList.withElements(SYNTACTIC_TYPE, SEMANTIC_TYPE);
    
    /**
     * Stores an immutable list with the person categories.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> INTERNAL_NON_HOST_IDENTITIES = ImmutableList.withElements(SYNTACTIC_TYPE, SEMANTIC_TYPE, NATURAL_PERSON, ARTIFICIAL_PERSON);
    
    /**
     * Stores an immutable list with the person categories.
     */
    public static final @Nonnull @NonNullableElements ImmutableList<Category> INTERNAL_IDENTITIES = ImmutableList.withElements(HOST, SYNTACTIC_TYPE, SEMANTIC_TYPE, NATURAL_PERSON, ARTIFICIAL_PERSON);
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns whether the given value denotes a valid category.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 6;
    }
    
    private final @Valid byte value;
    
    /**
     * Returns the byte representation of this category.
     */
    @Pure
    public @Valid byte getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private Category(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the category denoted by the given value.
     */
    @Pure
    @Recover
    public static @Nonnull Category of(@Valid byte value) {
        Require.that(isValid(value)).orThrow("The value has to be valid but was $.", value);
        
        for (@Nonnull Category category : values()) {
            if (category.value == value) { return category; }
        }
        
        throw CaseExceptionBuilder.withVariable("value").withValue(value).build();
    }
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    /**
     * Returns whether this category denotes a type.
     */
    @Pure
    public boolean isType() {
        return this == SYNTACTIC_TYPE || this == SEMANTIC_TYPE;
    }
    
    /**
     * Returns whether this category denotes an internal person.
     */
    @Pure
    public boolean isInternalPerson() {
        return this == NATURAL_PERSON || this == ARTIFICIAL_PERSON;
    }
    
    /**
     * Returns whether this category denotes an external person.
     */
    @Pure
    public boolean isExternalPerson() {
        return this == EMAIL_PERSON || this == MOBILE_PERSON;
    }
    
    /**
     * Returns whether this category denotes a person.
     */
    @Pure
    public boolean isPerson() {
        return isInternalPerson() || isExternalPerson();
    }
    
    /**
     * Returns whether this category denotes an internal non-host identity.
     */
    @Pure
    public boolean isInternalNonHostIdentity() {
        return isType()|| isInternalPerson();
    }
    
    /**
     * Returns whether this category denotes an internal identity.
     */
    @Pure
    public boolean isInternalIdentity() {
        return this == HOST || isInternalNonHostIdentity();
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return Strings.capitalizeFirstLetters(Strings.desnake(name()));
    }
    
}
