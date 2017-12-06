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
package net.digitalid.core.permissions;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCapturable;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.ReadOnly;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;

/**
 * This interface provides read-only access to {@link FreezableAgentPermissions agent permissions} and should <em>never</em> be cast away.
 */
@GenerateConverter
@ReadOnly(FreezableAgentPermissions.class)
public interface ReadOnlyAgentPermissions extends ReadOnlyMap<@Nonnull SemanticType, @Nonnull Boolean> {
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    @TODO(task = "Support the conversion of freezable collections. (This method is nonsense, of course.)", date = "2016-11-01", author = Author.KASPAR_ETTER)
    public static @Nonnull ReadOnlyAgentPermissions with(boolean frozen) {
        return FreezableAgentPermissions.withNoPermissions();
    }
    
    /* -------------------------------------------------- General Permission -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code general.permission.agent@core.digitalid.net}.
     */
    public static final @Nonnull @Loaded @AttributeType SemanticType GENERAL = SemanticType.map("general.permission.agent@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).withCategories(Category.INTERNAL_IDENTITIES).withCachingPeriod(Time.TROPICAL_YEAR).build());
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores an empty set of agent permissions.
     */
    public static final @Nonnull @Frozen ReadOnlyAgentPermissions NONE = FreezableAgentPermissions.withNoPermissions().freeze();
    
    /**
     * Stores a general read permission.
     */
    public static final @Nonnull @Frozen ReadOnlyAgentPermissions GENERAL_READ = FreezableAgentPermissions.withPermission(GENERAL, false).freeze();
    
    /**
     * Stores a general write permission.
     */
    public static final @Nonnull @Frozen ReadOnlyAgentPermissions GENERAL_WRITE = FreezableAgentPermissions.withPermission(GENERAL, true).freeze();
    
    /* -------------------------------------------------- Reading -------------------------------------------------- */
    
    /**
     * Returns whether these agent permissions allow to read the given type.
     */
    @Pure
    public default boolean allowToRead(@Nonnull @AttributeType SemanticType type) {
        return containsKey(type) || containsKey(GENERAL);
    }
    
    /**
     * Checks that these agent permissions allow to read the given type and throws a {@link RequestException} otherwise.
     */
    @Pure
    public default void checkAllowToRead(@Nonnull @AttributeType SemanticType type) throws RequestException {
        if (!allowToRead(type)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage(Strings.format("These agent permissions allow not to read $.", type.getAddress())).build(); }
    }
    
    /* -------------------------------------------------- Writing -------------------------------------------------- */
    
    /**
     * Returns whether these permissions allow to write the given type.
     */
    @Pure
    public default boolean allowToWrite(@Nonnull @AttributeType SemanticType type)  {
        return containsKey(type) && get(type) || containsKey(GENERAL) && get(GENERAL);
    }
    
    /**
     * Checks that these agent permissions allow to write the given type and throws a {@link RequestException} otherwise.
     */
    @Pure
    public default void checkAllowToWrite(@Nonnull @AttributeType SemanticType type) throws RequestException {
        if (!allowToWrite(type)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage(Strings.format("These agent permissions allow not to write $.", type.getAddress())).build(); }
    }
    
    /* -------------------------------------------------- Coverage -------------------------------------------------- */
    
    /**
     * Returns whether these agent permissions cover the given agent permissions.
     */
    @Pure
    public default boolean cover(@Nonnull ReadOnlyAgentPermissions permissions)  {
        final boolean generalPermission = containsKey(GENERAL);
        final boolean writingPermission = generalPermission ? get(GENERAL) : false;
        for (@Nonnull SemanticType type : permissions.keySet()) {
            if (containsKey(type)) {
                if (permissions.get(type) && !get(type)) { return false; }
            } else if (generalPermission) {
                if (permissions.get(type) && !writingPermission) { return false; }
            } else {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks that these agent permissions cover the given agent permissions and throws a {@link RequestException} otherwise.
     */
    @Pure
    public default void checkCover(@Nonnull ReadOnlyAgentPermissions permissions) throws RequestException {
        if (!cover(permissions)) { throw RequestExceptionBuilder.withCode(RequestErrorCode.AUTHORIZATION).withMessage(Strings.format("These agent permissions do not cover $.", permissions)).build(); }
    }
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Returns the readable types of these permissions (excluding the also writable types).
     */
    @Pure
    public default @NonCapturable @Nonnull FiniteIterable<@Nonnull SemanticType> readableTypes() {
        return entrySet().filter(entry -> !entry.getValue()).map(entry -> entry.getKey());
    }
    
    /**
     * Returns the writable types of these permissions.
     */
    @Pure
    public default @NonCapturable @Nonnull FiniteIterable<@Nonnull SemanticType> writableTypes() {
        return entrySet().filter(entry -> entry.getValue()).map(entry -> entry.getKey());
    }
    
    /* -------------------------------------------------- Cloneable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonFrozen FreezableAgentPermissions clone();
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    // TODO: Remove the following code if the database module can handle inline maps.
    
//    /**
//     * Sets the parameters at the given start index of the prepared statement to this object.
//     * 
//     * @param preparedStatement the prepared statement whose parameters are to be set.
//     * @param startIndex the start index of the parameters to set.
//     * 
//     * @require areEmptyOrSingle() : "These permissions are empty or single.";
//     */
//    @NonCommitting
//    public void setEmptyOrSingle(@Nonnull PreparedStatement preparedStatement, int startIndex) throws DatabaseException;
    
}
