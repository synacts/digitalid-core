package net.digitalid.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.contact.Context;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.external.AttributeNotFoundException;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.DataWrapper;
import net.digitalid.core.wrappers.ListWrapper;

/**
 * This class models a semantic type.
 * 
 * @invariant !isLoaded() || isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used as an attribute, the caching period is not null.";
 * @invariant !isLoaded() || getSyntacticBase().getNumberOfParameters() == -1 && getParameters().size() > 0 || getSyntacticBase().getNumberOfParameters() == getParameters().size() : "The number of required parameters is either variable or matches the given parameters.";
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class SemanticType extends Type implements Immutable {
    
    /**
     * Stores the semantic type {@code semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("semantic.type@core.digitalid.net").load(Type.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ATTRIBUTE_IDENTIFIER = SemanticType.create("attribute.type@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code role.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType ROLE_IDENTIFIER = SemanticType.create("role.type@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the semantic type {@code categories.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CATEGORIES = SemanticType.create("categories.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.TYPE, Category.TYPE);
    
    /**
     * Stores the semantic type {@code caching.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CACHING = SemanticType.create("caching.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, Time.TYPE);
    
    /**
     * Stores the semantic type {@code syntactic.base.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SYNTACTIC_BASE = SemanticType.create("syntactic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SyntacticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code parameters.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PARAMETERS = SemanticType.create("parameters.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.TYPE, SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code semantic.base.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SEMANTIC_BASE = SemanticType.create("semantic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the semantic type {@code unknown@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType UNKNOWN = SemanticType.create("unknown@core.digitalid.net").load(DataWrapper.TYPE);
    
    
    /**
     * Stores the categories for which this semantic type can be used as an attribute.
     * 
     * @invariant !isLoaded() || categories != null : "The categories are not null.";
     * @invariant !isLoaded() || categories.isFrozen() : "The categories are frozen.";
     * @invariant !isLoaded() || categories.doesNotContainNull() : "The categories do not contain null.";
     * @invariant !isLoaded() || categories.doesNotContainDuplicates() : "The categories do not contain duplicates.";
     */
    private @Nullable ReadonlyList<Category> categories;
    
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
     * @invariant !isLoaded() || parameters.isFrozen() : "The parameters are frozen.";
     * @invariant !isLoaded() || parameters.doesNotContainNull() : "The parameters do not contain null.";
     * @invariant !isLoaded() || parameters.doesNotContainDuplicates() : "The parameters do not contain duplicates.";
     */
    private @Nullable ReadonlyList<SemanticType> parameters;
    
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
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    SemanticType(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Creates a new semantic type with the given identifier.
     * 
     * @param identifier the identifier of the new semantic type.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    public static @Nonnull SemanticType create(@Nonnull String identifier) {
        return Mapper.mapSemanticType(new InternalNonHostIdentifier(identifier));
    }
    
    
    @Override
    @NonCommitting
    void load() throws SQLException, IOException, PacketException, ExternalException {
        assert isNotLoaded() : "The type declaration is not loaded.";
        
        if (categories != null) throw new InvalidEncodingException("The semantic base may not be circular.");
        
        Cache.getAttributeValues(this, null, Time.MIN, CATEGORIES, CACHING, SYNTACTIC_BASE, PARAMETERS, SEMANTIC_BASE);
        
        final @Nonnull ReadonlyList<Block> elements = new ListWrapper(Cache.getStaleAttributeContent(this, null, CATEGORIES)).getElementsNotNull();
        final @Nonnull FreezableList<Category> categories = new FreezableArrayList<Category>(elements.size());
        for (final @Nonnull Block element : elements) categories.add(Category.get(element));
        if (categories.doesNotContainDuplicates()) throw new InvalidEncodingException("The list of categories may not contain duplicates.");
        this.categories = categories.freeze();
        
        try {
            this.cachingPeriod = new Time(Cache.getStaleAttributeContent(this, null, CACHING));
            if (cachingPeriod.isNegative() || cachingPeriod.isGreaterThan(Time.TROPICAL_YEAR)) throw new InvalidEncodingException("The caching period must be null or non-negative and less than a year.");
        } catch (@Nonnull AttributeNotFoundException exception) {
            this.cachingPeriod = null;
        }
        if (categories.isNotEmpty() == (cachingPeriod == null)) throw new InvalidEncodingException("If (and only if) this semantic type can be used as an attribute, the caching period may not be null.");
        
        try {
            this.semanticBase = IdentifierClass.create(Cache.getStaleAttributeContent(this, null, SEMANTIC_BASE)).getIdentity().toSemanticType();
            this.syntacticBase = semanticBase.syntacticBase;
            this.parameters = semanticBase.parameters;
            setLoaded();
        } catch (@Nonnull AttributeNotFoundException exception) {
            this.syntacticBase = IdentifierClass.create(Cache.getStaleAttributeContent(this, null, SYNTACTIC_BASE)).getIdentity().toSyntacticType();
            final @Nonnull ReadonlyList<Block> list = new ListWrapper(Cache.getStaleAttributeContent(this, null, PARAMETERS)).getElementsNotNull();
            final @Nonnull FreezableList<SemanticType> parameters = new FreezableArrayList<SemanticType>(list.size());
            for (final @Nonnull Block element : elements) parameters.add(Mapper.getIdentity(IdentifierClass.create(element)).toSemanticType());
            if (parameters.doesNotContainDuplicates()) throw new InvalidEncodingException("The list of parameters may not contain duplicates.");
            if (!(syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size())) throw new InvalidEncodingException("The number of required parameters must either be variable or match the given parameters.");
            this.parameters = parameters.freeze();
            setLoaded();
            for (final @Nonnull SemanticType parameter : parameters) parameter.ensureLoaded();
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
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require categories.isFrozen() : "The categories have to be frozen.";
     * @require categories.doesNotContainNull() : "The categories may not contain null.";
     * @require categories.doesNotContainDuplicates() : "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * 
     * @require parameters.isFrozen() : "The parameters have to be frozen.";
     * @require parameters.doesNotContainNull() : "The parameters may not contain null.";
     * @require parameters.doesNotContainDuplicates() : "The parameters may not contain duplicates.";
     * 
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size() : "The number of required parameters has either to be variable or to match the given parameters.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull ReadonlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull ReadonlyList<SemanticType> parameters) {
        assert isNotLoaded() : "The type declaration is not loaded.";
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        assert categories.isFrozen() : "The categories have to be frozen.";
        assert categories.doesNotContainNull() : "The categories may not contain null.";
        assert categories.doesNotContainDuplicates() : "The categories may not contain duplicates.";
        
        assert cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
        
        assert parameters.isFrozen() : "The parameters have to be frozen.";
        assert parameters.doesNotContainNull() : "The parameters may not contain null.";
        assert parameters.doesNotContainDuplicates() : "The parameters may not contain duplicates.";
        
        assert categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
        assert syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size() : "The number of required parameters has either to be variable or to match the given parameters.";
        
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
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require new FreezableArray<Category>(categories).doesNotContainNull() : "The categories may not contain null.";
     * @require new FreezableArray<Category>(categories).doesNotContainDuplicates() : "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * 
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainNull() : "The parameters may not contain null.";
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainDuplicates() : "The parameters may not contain duplicates.";
     * 
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.length > 0 || syntacticBase.getNumberOfParameters() == parameters.length : "The number of required parameters has either to be variable or to match the given parameters.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull Category[] categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull SemanticType... parameters) {
        return load(new FreezableArray<Category>(categories).toFreezableList().freeze(), cachingPeriod, syntacticBase, new FreezableArray<SemanticType>(parameters).toFreezableList().freeze());
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param syntacticBase the syntactic type on which this semantic type is based.
     * @param parameters the generic parameters of the syntactic type.
     * 
     * @return this semantic type.
     * 
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainNull() : "The parameters may not contain null.";
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainDuplicates() : "The parameters may not contain duplicates.";
     * 
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.length > 0 || syntacticBase.getNumberOfParameters() == parameters.length : "The number of required parameters has either to be variable or to match the given parameters.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull SyntacticType syntacticBase, @Nonnull SemanticType... parameters) {
        return load(Category.NONE, null, syntacticBase, new FreezableArray<SemanticType>(parameters).toFreezableList().freeze());
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
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require categories.isFrozen() : "The categories have to be frozen.";
     * @require categories.doesNotContainNull() : "The categories may not contain null.";
     * @require categories.doesNotContainDuplicates() : "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * 
     * @require semanticBase.isLoaded() : "The semantic base is already loaded.";
     * 
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull ReadonlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull SemanticType semanticBase) {
        assert isNotLoaded() : "The type declaration is not loaded.";
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        assert categories.isFrozen() : "The categories have to be frozen.";
        assert categories.doesNotContainNull() : "The categories may not contain null.";
        assert categories.doesNotContainDuplicates() : "The categories may not contain duplicates.";
        
        assert cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
        
        assert semanticBase.isLoaded() : "The semantic base is already loaded.";
        
        assert categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
        
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
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require new FreezableArray<Category>(categories).doesNotContainNull() : "The categories may not contain null.";
     * @require new FreezableArray<Category>(categories).doesNotContainDuplicates() : "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * 
     * @require semanticBase.isLoaded() : "The semantic base is already loaded.";
     * 
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull Category[] categories, @Nullable Time cachingPeriod, @Nonnull SemanticType semanticBase) {
        return load(new FreezableArray<Category>(categories).toFreezableList().freeze(), cachingPeriod, semanticBase);
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param semanticBase the semantic type on which this semantic type is based.
     * 
     * @return this semantic type.
     * 
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require semanticBase.isLoaded() : "The semantic base is already loaded.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull SemanticType semanticBase) {
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
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure categories.isFrozen() : "The categories are frozen.";
     * @ensure categories.doesNotContainNull() : "The categories do not contain null.";
     * @ensure categories.doesNotContainDuplicates() : "The categories do not contain duplicates.";
     */
    @Pure
    public @Nonnull ReadonlyList<Category> getCategories() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert categories != null;
        return categories;
    }
    
    /**
     * Returns the caching period of this semantic type when used as an attribute.
     * 
     * @return the caching period of this semantic type when used as an attribute.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure return == null || return.isNonNegative() && return.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     */
    @Pure
    public @Nullable Time getCachingPeriod() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return cachingPeriod;
    }
    
    /**
     * Returns the caching period of this semantic type when used as an attribute.
     * 
     * @return the caching period of this semantic type when used as an attribute.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * @require getCachingPeriod() != null : "The caching period is not null.";
     * 
     * @ensure return.isNonNegative() && return.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is non-negative and less than a year.";
     */
    @Pure
    public @Nonnull Time getCachingPeriodNotNull() {
        assert isLoaded() : "The type declaration is already loaded.";
        @Nullable Time cachingPeriod = getCachingPeriod();
        assert cachingPeriod != null : "The caching period is not null.";
        
        return cachingPeriod;
    }
    
    /**
     * Returns the syntactic base of this semantic type.
     * 
     * @return the syntactic base of this semantic type.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public @Nonnull SyntacticType getSyntacticBase() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert syntacticBase != null;
        return syntacticBase;
    }
    
    /**
     * Returns the generic parameters of this semantic type.
     * 
     * @return the generic parameters of this semantic type.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure parameters.isFrozen() : "The parameters are frozen.";
     * @ensure parameters.doesNotContainNull() : "The parameters do not contain null.";
     * @ensure parameters.doesNotContainDuplicates() : "The parameters do not contain duplicates.";
     */
    @Pure
    public @Nonnull ReadonlyList<SemanticType> getParameters() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert parameters != null;
        return parameters;
    }
    
    /**
     * Returns the semantic base of this semantic type.
     * 
     * @return the semantic base of this semantic type.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure semanticBase == null || !semanticBase.isBasedOn(this) : "The semantic base is not based on this type.";
     */
    @Pure
    public @Nullable SemanticType getSemanticBase() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return semanticBase;
    }
    
    
    /**
     * Returns whether this semantic type can be used to denote an attribute.
     * 
     * @return whether this semantic type can be used to denote an attribute.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used to denote an attribute, the caching period is not null.";
     */
    @Pure
    public boolean isAttributeType() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert categories != null;
        return categories.isNotEmpty();
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidEncodingException if this is not the case.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public @Nonnull SemanticType checkIsAttributeType() throws InvalidEncodingException {
        if (!isAttributeType()) throw new InvalidEncodingException(getAddress() + " is not an attribute type.");
        return this;
    }
    
    /**
     * Returns whether this semantic type can be used to denote an attribute for the given category.
     * 
     * @param category the category of interest.
     * 
     * @return whether this semantic type can be used to denote an attribute for the given category.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public boolean isAttributeFor(@Nonnull Category category) {
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert categories != null;
        return categories.contains(category);
    }
    
    /**
     * Returns whether this semantic type can be used to denote an attribute for the given entity.
     * 
     * @param entity the entity of interest.
     * 
     * @return whether this semantic type can be used to denote an attribute for the given entity.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public boolean isAttributeFor(@Nonnull Entity entity) {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return isAttributeFor(entity.getIdentity().getCategory());
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute for the given entity.
     * 
     * @param entity the entity of interest.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidEncodingException if this is not the case.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public @Nonnull SemanticType checkIsAttributeFor(@Nonnull Entity entity) throws InvalidEncodingException {
        if (!isAttributeFor(entity)) throw new InvalidEncodingException(getAddress() + " is not an attribute for the given entity.");
        return this;
    }
    
    /**
     * Returns whether this semantic type is (indirectly) based on the given syntactic type.
     * 
     * @param syntacticType the syntactic type of interest.
     * 
     * @return whether this semantic type is (indirectly) based on the given syntactic type.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public boolean isBasedOn(@Nonnull SyntacticType syntacticType) {
        assert isLoaded() : "The type declaration is already loaded.";
        
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
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public boolean isBasedOn(@Nonnull SemanticType semanticType) {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return semanticType.equals(UNKNOWN) || equals(semanticType) || semanticBase != null && semanticBase.isBasedOn(semanticType);
    }
    
    /**
     * Returns whether this semantic type can be used to denote a role.
     * 
     * @return whether this semantic type can be used to denote a role.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public boolean isRoleType() {
        return isBasedOn(Context.FLAT);
    }
    
    /**
     * Checks that this semantic type can be used to denote a role.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidEncodingException if this is not the case.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public @Nonnull SemanticType checkIsRoleType() throws InvalidEncodingException {
        if (!isRoleType()) throw new InvalidEncodingException(getAddress() + " is not a role type.");
        return this;
    }
    
}
