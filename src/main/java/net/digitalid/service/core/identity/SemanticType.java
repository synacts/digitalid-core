package net.digitalid.service.core.identity;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.LoadedRecipient;
import net.digitalid.service.core.annotations.NonLoaded;
import net.digitalid.service.core.annotations.NonLoadedRecipient;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.contact.Context;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.exceptions.external.AttributeNotFoundException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.IdentifierClass;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.BytesWrapper;
import net.digitalid.service.core.wrappers.ListWrapper;
import net.digitalid.utility.annotations.math.NonNegative;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.elements.UniqueElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.annotations.OnMainThread;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a semantic type.
 * 
 * @invariant !isLoaded() || isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used as an attribute, the caching period is not null.";
 * @invariant !isLoaded() || getSyntacticBase().getNumberOfParameters() == -1 && getParameters().size() > 0 || getSyntacticBase().getNumberOfParameters() == getParameters().size() : "The number of required parameters is either variable or matches the given parameters.";
 */
@Immutable
public final class SemanticType extends Type {
    
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
     * Stores the semantic type {@code categories.attribute.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CATEGORIES = SemanticType.map("categories.attribute.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.TYPE, Category.TYPE);
    
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
    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, ListWrapper.TYPE, SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code semantic.base.semantic.type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SEMANTIC_BASE = SemanticType.map("semantic.base.semantic.type@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE}, Time.TROPICAL_YEAR, SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the semantic type {@code unknown@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType UNKNOWN = SemanticType.map("unknown@core.digitalid.net").load(BytesWrapper.TYPE);
    
    
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
    @OnMainThread
    @NonCommitting
    public static @Nonnull @NonLoaded SemanticType map(@Nonnull String identifier) {
        return Mapper.mapSemanticType(new InternalNonHostIdentifier(identifier));
    }
    
    
    @Override
    @NonCommitting
    @NonLoadedRecipient
    void load() throws AbortException, PacketException, ExternalException, NetworkException {
        assert !isLoaded() : "The type declaration is not loaded.";
        
        if (categories != null) throw new InvalidEncodingException("The semantic base may not be circular.");
        
        Cache.getAttributeValues(this, null, Time.MIN, CATEGORIES, CACHING, SYNTACTIC_BASE, PARAMETERS, SEMANTIC_BASE);
        
        final @Nonnull ReadOnlyList<Block> elements = new ListWrapper(Cache.getStaleAttributeContent(this, null, CATEGORIES)).getElementsNotNull();
        final @Nonnull FreezableList<Category> categories = new FreezableArrayList<>(elements.size());
        for (final @Nonnull Block element : elements) categories.add(Category.get(element));
        if (!categories.containsDuplicates()) throw new InvalidEncodingException("The list of categories may not contain duplicates.");
        this.categories = categories.freeze();
        
        try {
            this.cachingPeriod = new Time(Cache.getStaleAttributeContent(this, null, CACHING));
            if (cachingPeriod.isNegative() || cachingPeriod.isGreaterThan(Time.TROPICAL_YEAR)) throw new InvalidEncodingException("The caching period must be null or non-negative and less than a year.");
        } catch (@Nonnull AttributeNotFoundException exception) {
            this.cachingPeriod = null;
        }
        if (!categories.isEmpty() == (cachingPeriod == null)) throw new InvalidEncodingException("If (and only if) this semantic type can be used as an attribute, the caching period may not be null.");
        
        try {
            this.semanticBase = IdentifierClass.create(Cache.getStaleAttributeContent(this, null, SEMANTIC_BASE)).getIdentity().toSemanticType();
            this.syntacticBase = semanticBase.syntacticBase;
            this.parameters = semanticBase.parameters;
            setLoaded();
        } catch (@Nonnull AttributeNotFoundException exception) {
            this.syntacticBase = IdentifierClass.create(Cache.getStaleAttributeContent(this, null, SYNTACTIC_BASE)).getIdentity().toSyntacticType();
            final @Nonnull ReadOnlyList<Block> list = new ListWrapper(Cache.getStaleAttributeContent(this, null, PARAMETERS)).getElementsNotNull();
            final @Nonnull FreezableList<SemanticType> parameters = new FreezableArrayList<>(list.size());
            for (final @Nonnull Block element : elements) parameters.add(Mapper.getIdentity(IdentifierClass.create(element)).toSemanticType());
            if (!parameters.containsDuplicates()) throw new InvalidEncodingException("The list of parameters may not contain duplicates.");
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
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * 
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size() : "The number of required parameters has either to be variable or to match the given parameters.";
     */
    @OnMainThread
    @NonLoadedRecipient
    public @Nonnull SemanticType load(@Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<SemanticType> parameters) {
        assert !isLoaded() : "The type declaration is not loaded.";
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        assert categories.isFrozen() : "The categories have to be frozen.";
        assert !categories.containsNull(): "The categories may not contain null.";
        assert !categories.containsDuplicates(): "The categories may not contain duplicates.";
        
        assert cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
        
        assert parameters.isFrozen() : "The parameters have to be frozen.";
        assert !parameters.containsNull(): "The parameters may not contain null.";
        assert !parameters.containsDuplicates(): "The parameters may not contain duplicates.";
        
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
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.length > 0 || syntacticBase.getNumberOfParameters() == parameters.length : "The number of required parameters has either to be variable or to match the given parameters.";
     */
    @OnMainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @NonNullableElements @UniqueElements Category[] categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull @NonNullableElements @UniqueElements SemanticType... parameters) {
        return load(new FreezableArray<>(categories).toFreezableList().freeze(), cachingPeriod, syntacticBase, new FreezableArray<>(parameters).toFreezableList().freeze());
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
    @OnMainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull SyntacticType syntacticBase, @Nonnull @NonNullableElements @UniqueElements SemanticType... parameters) {
        return load(Category.NONE, null, syntacticBase, new FreezableArray<>(parameters).toFreezableList().freeze());
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
    @OnMainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @Frozen @NonNullableElements @UniqueElements ReadOnlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull @Loaded SemanticType semanticBase) {
        assert !isLoaded() : "The type declaration is not loaded.";
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        assert categories.isFrozen() : "The categories have to be frozen.";
        assert !categories.containsNull() : "The categories may not contain null.";
        assert !categories.containsDuplicates() : "The categories may not contain duplicates.";
        
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
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() && cachingPeriod.isLessThanOrEqualTo(Time.TROPICAL_YEAR) : "The caching period is null or non-negative and less than a year.";
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     */
    @OnMainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SemanticType load(@Nonnull @NonNullableElements @UniqueElements Category[] categories, @Nullable Time cachingPeriod, @Nonnull @Loaded SemanticType semanticBase) {
        return load(new FreezableArray<>(categories).toFreezableList().freeze(), cachingPeriod, semanticBase);
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param semanticBase the semantic type on which this semantic type is based.
     * 
     * @return this semantic type.
     */
    @OnMainThread
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
        assert isLoaded() : "The type declaration is already loaded.";
        
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
        assert isLoaded() : "The type declaration is already loaded.";
        
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
        assert isLoaded() : "The type declaration is already loaded.";
        @Nullable Time cachingPeriod = getCachingPeriod();
        assert cachingPeriod != null : "The caching period is not null.";
        
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
        assert isLoaded() : "The type declaration is already loaded.";
        
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
        assert isLoaded() : "The type declaration is already loaded.";
        
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
        assert isLoaded() : "The type declaration is already loaded.";
        
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
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert categories != null;
        return !categories.isEmpty();
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute.
     * 
     * @return this semantic type.
     * 
     * @throws InvalidEncodingException if this is not the case.
     */
    @Pure
    @LoadedRecipient
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
     */
    @Pure
    @LoadedRecipient
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
     */
    @Pure
    @LoadedRecipient
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
     */
    @Pure
    @LoadedRecipient
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
     */
    @Pure
    @LoadedRecipient
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
     */
    @Pure
    @LoadedRecipient
    public boolean isBasedOn(@Nonnull SemanticType semanticType) {
        assert isLoaded() : "The type declaration is already loaded.";
        
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
     * @throws InvalidEncodingException if this is not the case.
     */
    @Pure
    @LoadedRecipient
    public @Nonnull SemanticType checkIsRoleType() throws InvalidEncodingException {
        if (!isRoleType()) throw new InvalidEncodingException(getAddress() + " is not a role type.");
        return this;
    }
    
}
