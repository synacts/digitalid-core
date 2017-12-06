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
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.math.relative.GreaterThanOrEqualTo;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

/**
 * The type loader loads the attributes of a type.
 */
@Stateless
public interface TypeLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
//    /**
//     * Stores the semantic type {@code parameters.syntactic.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.syntactic.type@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE}, Time.TROPICAL_YEAR, Integer08Wrapper.XDF_TYPE);
    
    /**
     * Loads and returns the number of generic parameters of the given syntactic type.
     */
    @Pure
    @NonCommitting
    public @GreaterThanOrEqualTo(-1) byte load(@Nonnull SyntacticType syntacticType) throws ExternalException;
//        this.numberOfParameters = Integer08Wrapper.decode(Cache.getStaleAttributeContent(this, null, PARAMETERS));
//        if (numberOfParameters < -1) { throw InvalidDeclarationException.get("The number of parameters has to be at least -1 but was " + numberOfParameters + ".", getAddress()); }
    
//    /**
//     * Stores the semantic type {@code categories.attribute.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType CATEGORIES = SemanticType.map("categories.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.XDF_TYPE, Category.TYPE);
//    
//    /**
//     * Stores the semantic type {@code caching.attribute.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType CACHING = SemanticType.map("caching.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, Time.TYPE);
//    
//    /**
//     * Stores the semantic type {@code syntactic.base.semantic.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType SYNTACTIC_BASE = SemanticType.map("syntactic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SyntacticType.IDENTIFIER);
//    
//    /**
//     * Stores the semantic type {@code parameters.semantic.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.XDF_TYPE, SemanticType.IDENTIFIER);
//    
//    /**
//     * Stores the semantic type {@code semantic.base.semantic.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType SEMANTIC_BASE = SemanticType.map("semantic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SemanticType.IDENTIFIER);
    
    /**
     * Loads the attributes of the given semantic type.
     */
    @Pure
    @NonCommitting
    public @Nonnull SemanticTypeAttributes load(@Nonnull SemanticType semanticType) throws ExternalException;
//        if (categories != null) { throw InvalidDeclarationException.get("The semantic base may not be circular.", getAddress()); }
//        
//        Cache.getAttributeValues(this, null, Time.MIN, CATEGORIES, CACHING, SYNTACTIC_BASE, PARAMETERS, SEMANTIC_BASE);
//        
//        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(Cache.getStaleAttributeContent(this, null, CATEGORIES));
//        final @Nonnull FreezableList<Category> categories = FreezableArrayList.getWithCapacity(elements.size());
//        for (final @Nonnull Block element : elements) { categories.add(Category.get(element)); }
//        if (!categories.containsDuplicates()) { throw InvalidParameterValueException.get("categories", categories); }
//        this.categories = categories.freeze();
//        
//        try {
//            this.cachingPeriod = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getStaleAttributeContent(this, null, CACHING));
//            if (cachingPeriod.isNegative() || cachingPeriod.isGreaterThan(Time.TROPICAL_YEAR)) { throw InvalidParameterValueException.get("caching period", cachingPeriod); }
//        } catch (@Nonnull AttributeNotFoundException exception) {
//            this.cachingPeriod = null;
//        }
//        if (!categories.isEmpty() == (cachingPeriod == null)) { throw InvalidParameterValueCombinationException.get("If (and only if) this semantic type can be used as an attribute, the caching period may not be null."); }
//        
//        try {
//            this.semanticBase = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getStaleAttributeContent(this, null, SEMANTIC_BASE)).getIdentity().castTo(SemanticType.class);
//            this.syntacticBase = semanticBase.syntacticBase;
//            this.parameters = semanticBase.parameters;
//            setLoaded();
//        } catch (@Nonnull AttributeNotFoundException exception) {
//            this.syntacticBase = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getStaleAttributeContent(this, null, SYNTACTIC_BASE)).getIdentity().castTo(SyntacticType.class);
//            final @Nonnull ReadOnlyList<Block> list = ListWrapper.decodeNonNullableElements(Cache.getStaleAttributeContent(this, null, PARAMETERS));
//            final @Nonnull FreezableList<SemanticType> parameters = FreezableArrayList.getWithCapacity(list.size());
//            for (final @Nonnull Block element : elements) { parameters.add(Mapper.getIdentity(IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, element)).castTo(SemanticType.class)); }
//            if (!parameters.containsDuplicates()) { throw InvalidParameterValueException.get("parameters", parameters); }
//            if (!(syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size())) { throw InvalidParameterValueCombinationException.get("The number of required parameters must either be variable or match the given parameters."); }
//            this.parameters = parameters.freeze();
//            setLoaded();
//            for (final @Nonnull SemanticType parameter : parameters) { parameter.ensureLoaded(); }
//        }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the type loader, which has to be provided by another package.
     */
    public static final @Nonnull Configuration<TypeLoader> configuration = Configuration.withUnknownProvider();
    
}
