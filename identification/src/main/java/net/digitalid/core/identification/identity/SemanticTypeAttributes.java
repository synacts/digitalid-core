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
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.UniqueElements;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.math.relative.LessThanOrEqualTo;
import net.digitalid.utility.validation.annotations.size.Empty;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.annotations.type.Loaded;

/**
 * This class combines the attributes of a {@link SemanticType}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class SemanticTypeAttributes extends RootClass {
    
    /* -------------------------------------------------- Categories -------------------------------------------------- */
    
    /**
     * Returns the categories for which the semantic type can be used as an attribute.
     */
    @Pure
    @Default("Category.NONE")
    public abstract @Nonnull @NonNullableElements @UniqueElements ImmutableList<Category> getCategories();
    
    /* -------------------------------------------------- Caching Period -------------------------------------------------- */
    
    /**
     * Returns the caching period of the semantic type when used as an attribute.
     */
    @Pure
    @Default("Time.MIN")
    public abstract @Nonnull @NonNegative @LessThanOrEqualTo(/* Time.TROPICAL_YEAR: */ 31_556_925_190l) Time getCachingPeriod();
    
    /* -------------------------------------------------- Syntactic Base -------------------------------------------------- */
    
    /**
     * Returns the syntactic base of the semantic type.
     */
    @Pure
    public abstract @Nonnull @Loaded SyntacticType getSyntacticBase();
    
    /* -------------------------------------------------- Parameters -------------------------------------------------- */
    
    public static final @Nonnull @Empty ImmutableList<SemanticType> NONE = ImmutableList.withElements();
    
    /**
     * Returns the generic parameters of the semantic type.
     */
    @Pure
    @Default("SemanticTypeAttributes.NONE")
    public abstract @Nonnull @NonNullableElements @UniqueElements @Invariant(condition = "syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size()", message = "The number of required parameters has either to be variable or to match the given parameters.") ImmutableList<SemanticType> getParameters();
    
    /* -------------------------------------------------- Semantic Base -------------------------------------------------- */
    
    /**
     * Returns the semantic base of the semantic type.
     */
    @Pure
    public abstract @Nullable @Loaded SemanticType getSemanticBase();
    
}
