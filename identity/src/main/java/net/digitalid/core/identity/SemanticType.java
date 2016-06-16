package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.conversion.None;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.UniqueElements;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.cache.Cache;
import net.digitalid.core.cache.exceptions.AttributeNotFoundException;
import net.digitalid.core.contact.Context;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.Converters;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.value.binary.BinaryWrapper;
import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.core.conversion.xdf.ChainingRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.RequestingXDFConverter;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;
import net.digitalid.core.identifier.Identifier;
import net.digitalid.core.identifier.IdentifierImplementation;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identity.annotations.Loaded;
import net.digitalid.core.identity.annotations.LoadedRecipient;
import net.digitalid.core.identity.annotations.NonLoaded;
import net.digitalid.core.identity.annotations.NonLoadedRecipient;
import net.digitalid.core.packet.exceptions.InvalidDeclarationException;
import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;

/**
 * This class models a semantic type.
 * 
 * @invariant !isLoaded() || isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used as an attribute, the caching period is not null.";
 * @invariant !isLoaded() || getSyntacticBase().getNumberOfParameters() == -1 && getParameters().size() > 0 || getSyntacticBase().getNumberOfParameters() == getParameters().size() : "The number of required parameters is either variable or matches the given parameters.";
 */
@Immutable
public final class SemanticType extends Type {
    
    /**
     * Stores the semantic type {@code categories.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CATEGORIES = SemanticType.map("categories.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.XDF_TYPE, Category.TYPE);
    
    /**
     * Stores the semantic type {@code caching.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CACHING = SemanticType.map("caching.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, Time.TYPE);
    
    /**
     * Stores the semantic type {@code syntactic.base.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SYNTACTIC_BASE = SemanticType.map("syntactic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SyntacticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code parameters.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.XDF_TYPE, SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code semantic.base.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SEMANTIC_BASE = SemanticType.map("semantic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the semantic type {@code unknown@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType UNKNOWN = SemanticType.map("unknown@core.digitalid.net").load(BinaryWrapper.XDF_TYPE);
    
    
    /**
     * Stores the categories for which this semantic type can be used as an attribute.
     * 
     * @invariant !isLoaded() || categories != null : "The categories are not null.";
     */
    private @Nullable @Frozen @NonNullableElements @UniqueElements ReadOnlyList<Category> categories;
    
    /**
     * Stores the caching period of this semantic type when used as an attribute.
     * 
     * @invariant cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     */
    private @Nullable Time cachingPeriod;
    
    /**
     * Stores the syntactic base of this semantic type.
     * 
     * @invariant !isLoaded() || syntacticBase != null : "The syntactic base is not null.";
     */
    private @Nullable SyntacticType syntacticBase;
    
    /**
     * Stores the generic parameters of this semantic type.
     * 
     * @invariant !isLoaded() || parameters != null : "The parameters are not null.";
     */
    private @Nullable @Frozen @NonNullableElements @UniqueElements ReadOnlyList<SemanticType> parameters;
    
    /**
     * Stores the semantic base of this semantic type.
     * 
     * @invariant semanticBase == null || !semanticBase.isBasedOn(this) : "The semantic base is not based on this type.";
     */
    private @Nullable SemanticType semanticBase;
    
    /**
     * Creates a new semantic type with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    @NonLoaded SemanticType(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Maps the semantic type with the given identifier.
     * 
     * @param identifier the identifier of the semantic type.
     * 
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     */
    @MainThread
    @NonCommitting
    public static @Nonnull @NonLoaded SemanticType map(@Nonnull String identifier) {
        return Mapper.mapSemanticType(InternalNonHostIdentifier.get(identifier));
    }
    
    
    @Override
    @NonCommitting
    @NonLoadedRecipient
    void load() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(!isLoaded()).orThrow("The type declaration is not loaded.");
        
        if (categories != null) { throw InvalidDeclarationException.get("The semantic base may not be circular.", getAddress()); }
        
        Cache.getAttributeValues(this, null, Time.MIN, CATEGORIES, CACHING, SYNTACTIC_BASE, PARAMETERS, SEMANTIC_BASE);
        
        final @Nonnull ReadOnlyList<Block> elements = ListWrapper.decodeNonNullableElements(Cache.getStaleAttributeContent(this, null, CATEGORIES));
        final @Nonnull FreezableList<Category> categories = FreezableArrayList.getWithCapacity(elements.size());
        for (final @Nonnull Block element : elements) { categories.add(Category.get(element)); }
        if (!categories.containsDuplicates()) { throw InvalidParameterValueException.get("categories", categories); }
        this.categories = categories.freeze();
        
        try {
            this.cachingPeriod = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getStaleAttributeContent(this, null, CACHING));
            if (cachingPeriod.isNegative() || cachingPeriod.isGreaterThan(Time.TROPICAL_YEAR)) { throw InvalidParameterValueException.get("caching period", cachingPeriod); }
        } catch (@Nonnull AttributeNotFoundException exception) {
            this.cachingPeriod = null;
        }
        if (!categories.isEmpty() == (cachingPeriod == null)) { throw InvalidParameterValueCombinationException.get("If (and only if) this semantic type can be used as an attribute, the caching period may not be null."); }
        
        try {
            this.semanticBase = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getStaleAttributeContent(this, null, SEMANTIC_BASE)).getIdentity().castTo(SemanticType.class);
            this.syntacticBase = semanticBase.syntacticBase;
            this.parameters = semanticBase.parameters;
            setLoaded();
        } catch (@Nonnull AttributeNotFoundException exception) {
            this.syntacticBase = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getStaleAttributeContent(this, null, SYNTACTIC_BASE)).getIdentity().castTo(SyntacticType.class);
            final @Nonnull ReadOnlyList<Block> list = ListWrapper.decodeNonNullableElements(Cache.getStaleAttributeContent(this, null, PARAMETERS));
            final @Nonnull FreezableList<SemanticType> parameters = FreezableArrayList.getWithCapacity(list.size());
            for (final @Nonnull Block element : elements) { parameters.add(Mapper.getIdentity(IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, element)).castTo(SemanticType.class)); }
            if (!parameters.containsDuplicates()) { throw InvalidParameterValueException.get("parameters", parameters); }
            if (!(syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size())) { throw InvalidParameterValueCombinationException.get("The number of required parameters must either be variable or match the given parameters."); }
            this.parameters = parameters.freeze();
            setLoaded();
            for (final @Nonnull SemanticType parameter : parameters) { parameter.ensureLoaded(); }
        }
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param categories the categories for which this semantic type can be used as an attribute.
     * @param cachingPeriod the caching period of this semantic type when used as an attribute.
     * @param syntacticBase the syntactic type on which this semantic type is based.
     * @param parameters the generic parameters of the syntactic type.
     * 
     * @return this semantic type.
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * 
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size() : "The number of required parameters has either to be variable or to match the given parameters.";
     */
    @MainThread
    @NonLoadedRecipient
    public @Nonnull SemanticType load(@Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<SemanticType> parameters) {
        Require.that(!isLoaded()).orThrow("The type declaration is not loaded.");
        Require.that(Threading.isMainThread()).orThrow("This method may only be called in the main thread.");
        
        Require.that(categories.isFrozen()).orThrow("The categories have to be frozen.");
        assert !categories.containsNull(): "The categories may not contain null.";
        assert !categories.containsDuplicates(): "The categories may not contain duplicates.";
        
        Require.that(cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR)).orThrow("The caching period is null or non-negative and less than a year.");
        
        Require.that(parameters.isFrozen()).orThrow("The parameters have to be frozen.");
        assert !parameters.containsNull(): "The parameters may not contain null.";
        assert !parameters.containsDuplicates(): "The parameters may not contain duplicates.";
        
        Require.that(categories.isEmpty() == (cachingPeriod == null)).orThrow("The caching period is null if and only if the categories are empty.");
        Require.that(syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size()).orThrow("The number of required parameters has either to be variable or to match the given parameters.");
        
        this.categories = categories;
        this.cachingPeriod = cachingPeriod;
        this.syntacticBase = syntacticBase;
        this.parameters = parameters;
        this.semanticBase = null;
        setLoaded();
        
        return this;
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param categories the categories for which this semantic type can be used as an attribute.
     * @param cachingPeriod the caching period of this semantic type when used as an attribute.
     * @param syntacticBase the syntactic type on which this semantic type is based.
     * @param parameters the generic parameters of the syntactic type.
     * 
     * @return this semantic type.
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.length > 0 || syntacticBase.getNumberOfParameters() == parameters.length : "The number of required parameters has either to be variable or to match the given parameters.";
     */
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @NonNullableElements @UniqueElements Category[] categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull @NonNullableElements @UniqueElements SemanticType... parameters) {
        return load(FreezableArray.getNonNullable(categories).toFreezableList().freeze(), cachingPeriod, syntacticBase, FreezableArray.getNonNullable(parameters).toFreezableList().freeze());
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param syntacticBase the syntactic type on which this semantic type is based.
     * @param parameters the generic parameters of the syntactic type.
     * 
     * @return this semantic type.
     * 
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.length > 0 || syntacticBase.getNumberOfParameters() == parameters.length : "The number of required parameters has either to be variable or to match the given parameters.";
     */
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull SyntacticType syntacticBase, @Nonnull @NonNullableElements @UniqueElements SemanticType... parameters) {
        return load(Category.NONE, null, syntacticBase, FreezableArray.getNonNullable(parameters).toFreezableList().freeze());
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param categories the categories for which this semantic type can be used as an attribute.
     * @param cachingPeriod the caching period of this semantic type when used as an attribute.
     * @param semanticBase the semantic type on which this semantic type is based.
     * 
     * @return this semantic type.
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     */
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull @Loaded SemanticType semanticBase) {
        Require.that(!isLoaded()).orThrow("The type declaration is not loaded.");
        Require.that(Threading.isMainThread()).orThrow("This method may only be called in the main thread.");
        
        Require.that(categories.isFrozen()).orThrow("The categories have to be frozen.");
        Require.that(!categories.containsNull()).orThrow("The categories may not contain null.");
        Require.that(!categories.containsDuplicates()).orThrow("The categories may not contain duplicates.");
        
        Require.that(cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR)).orThrow("The caching period is null or non-negative and less than a year.");
        
        Require.that(semanticBase.isLoaded()).orThrow("The semantic base is already loaded.");
        
        Require.that(categories.isEmpty() == (cachingPeriod == null)).orThrow("The caching period is null if and only if the categories are empty.");
        
        this.categories = categories;
        this.cachingPeriod = cachingPeriod;
        this.syntacticBase = semanticBase.syntacticBase;
        this.parameters = semanticBase.parameters;
        this.semanticBase = semanticBase;
        setLoaded();
        
        return this;
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param categories the categories for which this semantic type can be used as an attribute.
     * @param cachingPeriod the caching period of this semantic type when used as an attribute.
     * @param semanticBase the semantic type on which this semantic type is based.
     * 
     * @return this semantic type.
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     */
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @NonNullableElements @UniqueElements Category[] categories, @Nullable Time cachingPeriod, @Nonnull @Loaded SemanticType semanticBase) {
        return load(FreezableArray.getNonNullable(categories).toFreezableList().freeze(), cachingPeriod, semanticBase);
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param semanticBase the semantic type on which this semantic type is based.
     * 
     * @return this semantic type.
     */
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @Loaded SemanticType semanticBase) {
        return load(Category.NONE, null, semanticBase);
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SEMANTIC_TYPE;
    }
    
    
    /**
     * Returns the categories for which this semantic type can be used as an attribute.
     * 
     * @return the categories for which this semantic type can be used as an attribute.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<Category> getCategories() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        assert categories != null;
        return categories;
    }
    
    /**
     * Returns the caching period of this semantic type when used as an attribute.
     * 
     * @return the caching period of this semantic type when used as an attribute.
     * 
     * @ensure return == null || return.isNonNegative() && return.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     */
    @Pure
    @LoadedRecipient
    public @Nullable @NonNegative Time getCachingPeriod() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        return cachingPeriod;
    }
    
    /**
     * Returns the caching period of this semantic type when used as an attribute.
     * 
     * @return the caching period of this semantic type when used as an attribute.
     * 
     * @require getCachingPeriod() != null : "The caching period is not null.";
     * 
     * @ensure return.isNonNegative() && return.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is non-negative and less than a year.";
     */
    @Pure
    @LoadedRecipient
    public @Nonnull @NonNegative Time getCachingPeriodNotNull() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        @Nullable Time cachingPeriod = getCachingPeriod();
        Require.that(cachingPeriod != null).orThrow("The caching period is not null.");
        
        return cachingPeriod;
    }
    
    /**
     * Returns the syntactic base of this semantic type.
     * 
     * @return the syntactic base of this semantic type.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull SyntacticType getSyntacticBase() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        assert syntacticBase != null;
        return syntacticBase;
    }
    
    /**
     * Returns the generic parameters of this semantic type.
     * 
     * @return the generic parameters of this semantic type.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<SemanticType> getParameters() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        assert parameters != null;
        return parameters;
    }
    
    /**
     * Returns the semantic base of this semantic type.
     * 
     * @return the semantic base of this semantic type.
     * 
     * @ensure semanticBase == null || !semanticBase.isBasedOn(this) : "The semantic base is not based on this type.";
     */
    @Pure
    @LoadedRecipient
    public @Nullable SemanticType getSemanticBase() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        return semanticBase;
    }
    
    
    /**
     * Returns whether this semantic type can be used to denote an attribute.
     * 
     * @return whether this semantic type can be used to denote an attribute.
     * 
     * @ensure isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used to denote an attribute, the caching period is not null.";
     */
    @Pure
    @LoadedRecipient
    public boolean isAttributeType() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        assert categories != null;
        return !categories.isEmpty();
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidParameterValueException if this is not the case.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull SemanticType checkIsAttributeType() throws InvalidParameterValueException {
        if (!isAttributeType()) { throw InvalidParameterValueException.get("attribute type", this); }
        return this;
    }
    
    /**
     * Returns whether this semantic type can be used to denote an attribute for the given category.
     * 
     * @param category the category of interest.
     * 
     * @return whether this semantic type can be used to denote an attribute for the given category.
     */
    @Pure
    @LoadedRecipient
    public boolean isAttributeFor(@Nonnull Category category) {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        assert categories != null;
        return categories.contains(category);
    }
    
    /**
     * Returns whether this semantic type can be used to denote an attribute for the given entity.
     * 
     * @param entity the entity of interest.
     * 
     * @return whether this semantic type can be used to denote an attribute for the given entity.
     */
    @Pure
    @LoadedRecipient
    public boolean isAttributeFor(@Nonnull Entity entity) {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        return isAttributeFor(entity.getIdentity().getCategory());
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute for the given entity.
     * 
     * @param entity the entity of interest.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidParameterValueCombinationException if this is not the case.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull SemanticType checkIsAttributeFor(@Nonnull Entity entity) throws InvalidParameterValueCombinationException {
        if (!isAttributeFor(entity)) { throw InvalidParameterValueCombinationException.get(getAddress() + " is not an attribute for the entity " + entity.getIdentity().getAddress() + "."); }
        return this;
    }
    
    /**
     * Returns whether this semantic type is (indirectly) based on the given syntactic type.
     * 
     * @param syntacticType the syntactic type of interest.
     * 
     * @return whether this semantic type is (indirectly) based on the given syntactic type.
     */
    @Pure
    @LoadedRecipient
    public boolean isBasedOn(@Nonnull SyntacticType syntacticType) {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        assert syntacticBase != null;
        return syntacticBase.equals(syntacticType);
    }
    
    /**
     * Returns whether this semantic type is (indirectly) based on the given semantic type.
     * This relation is reflexive, transitive and antisymmetric.
     * 
     * @param semanticType the semantic type of interest.
     * 
     * @return whether this semantic type is (indirectly) based on the given semantic type.
     */
    @Pure
    @LoadedRecipient
    public boolean isBasedOn(@Nonnull SemanticType semanticType) {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        return semanticType.equals(UNKNOWN) || equals(semanticType) || semanticBase != null && semanticBase.isBasedOn(semanticType);
    }
    
    /**
     * Returns whether this semantic type can be used to denote a role.
     * 
     * @return whether this semantic type can be used to denote a role.
     */
    @Pure
    @LoadedRecipient
    public boolean isRoleType() {
        return isBasedOn(Context.FLAT);
    }
    
    /**
     * Checks that this semantic type can be used to denote a role.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidParameterValueException if this is not the case.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull SemanticType checkIsRoleType() throws InvalidParameterValueException {
        if (!isRoleType()) { throw InvalidParameterValueException.get("role type", this); }
        return this;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("semantic.type@core.digitalid.net").load(Type.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ATTRIBUTE_IDENTIFIER = SemanticType.map("attribute.type@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code role.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ROLE_IDENTIFIER = SemanticType.map("role.type@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<SemanticType, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(new Identity.IdentifierConverter<>(SemanticType.class), Identifier.XDF_CONVERTER);
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("semantic_type", false);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<SemanticType, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(SemanticType.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<SemanticType, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}