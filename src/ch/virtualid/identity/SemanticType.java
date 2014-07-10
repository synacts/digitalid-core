package ch.virtualid.identity;

import ch.virtualid.annotation.Pure;
import ch.virtualid.concepts.Context;
import ch.virtualid.concepts.Time;
import ch.virtualid.database.Database;
import ch.virtualid.exception.InvalidDeclarationException;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.DataWrapper;
import ch.xdf.ListWrapper;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the semantic type virtual identities.
 * 
 * @invariant !isLoaded() || isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used as an attribute, the caching period is not null.";
 * @invariant !isLoaded() || getSyntacticBase().getNumberOfParameters() == -1 && parameters.size() > 0 || getSyntacticBase().getNumberOfParameters() == getParameters().size() : "The number of required parameters is either variable or matches the given parameters.";
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public final class SemanticType extends Type implements Immutable {
    
    /**
     * Stores the semantic type {@code semantic.type@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("semantic.type@virtualid.ch").load(Type.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code categories.attribute.type@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CATEGORIES = SemanticType.create("categories.attribute.type@virtualid.ch").load(ListWrapper.TYPE, Category.TYPE);
    
    /**
     * Stores the semantic type {@code caching.attribute.type@virtualid.ch}.
     */
    private static final @Nonnull SemanticType CACHING = SemanticType.create("caching.attribute.type@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code syntactic.base.semantic.type@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SYNTACTIC_BASE = SemanticType.create("syntactic.base.semantic.type@virtualid.ch").load(SyntacticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code parameters.semantic.type@virtualid.ch}.
     */
    private static final @Nonnull SemanticType PARAMETERS = SemanticType.create("parameters.semantic.type@virtualid.ch").load(ListWrapper.TYPE, SyntacticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code semantic.base.semantic.type@virtualid.ch}.
     */
    private static final @Nonnull SemanticType SEMANTIC_BASE = SemanticType.create("semantic.base.semantic.type@virtualid.ch").load(SemanticType.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code unknown@virtualid.ch}.
     */
    public static final @Nonnull SemanticType UNKNOWN = SemanticType.create("unknown@virtualid.ch").load(DataWrapper.TYPE);
    
    
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
     * @invariant cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
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
    SemanticType(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Creates a new semantic type with the given identifier.
     * 
     * @param identifier the identifier of the new semantic type.
     * 
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require !Identifier.isHost(identifier) : "The string may not denote a host identifier.";
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    public static @Nonnull SemanticType create(@Nonnull String identifier) {
        return Mapper.mapSemanticType(new NonHostIdentifier(identifier));
    }
    
    
    /**
     * Returns whether the type declaration has already been loaded.
     * 
     * @return whether the type declaration has already been loaded.
     */
    private boolean isLoading() {
        return !isLoaded() && categories != null;
    }
    
    @Override
    void load() throws SQLException, InvalidDeclarationException, FailedIdentityException {
        assert !isLoaded() : "The type declaration may not yet have been loaded.";
        
        todo;
        
        /*
        
        // TODO: Make a single lookup for all desired attributes of this semantic type.
        
        // If a semantic base is loaded, make sure that it !isLoading() and throw an InvalidDeclarationException otherwise.
        
        // TODO: Tolerate stale attributes from the cache! (Might not be necessary, as types are usually only checked in assertions.)
        
        // Return InvalidEncodingExceptions as InvalidDeclarationException.
        
        // categories
        final @Nullable Block value = Client.getAttributeUnwrapped(this, SemanticType.ATTRIBUTE_TYPE_CATEGORIES);
        if (value != null) {
            final @Nonnull List<Block> elements = new ListWrapper(value).getElements();
            for (final @Nonnull Block element : elements) {
                if (Category.get(element) == category) return true;
            }
        }
        
        // syntacticBase
        final @Nullable Block syntacticBase = Client.getAttributeUnwrapped(this, SEMANTIC_TYPE_SYNTACTIC_BASE);
        if (syntacticBase != null) {
            final @Nullable Block syntacticBase = Client.getAttributeUnwrapped(this, SEMANTIC_TYPE_SYNTACTIC_BASE);
            
            new NonHostIdentifier(syntacticBase).getIdentity()
            
        } else {
            syntacticBase = Client.getAttributeUnwrapped(this, SEMANTIC_TYPE_SEMANTIC_BASE);
            if (syntacticBase == null) {
                throw new InvalidDeclarationException(getAddress() + " does neither specify a base nor an inheritance.");
            } else {
                return new NonHostIdentifier(syntacticBase).getIdentity().toSemanticType().isBasedOn(syntacticType);
            }
        } else {
            return new NonHostIdentifier(syntacticBase).getIdentity().equals(syntacticType);
        }
        
        // parameters
        @Nullable Block value = Client.getAttributeUnwrapped(this, SEMANTIC_TYPE_PARAMETERS);
        if (value == null) {
            value = Client.getAttributeUnwrapped(this, SEMANTIC_TYPE_SEMANTIC_BASE);
            if (value == null) {
                throw new InvalidDeclarationException(getAddress() + " does neither specify a base nor an inheritance.");
            } else {
                return new NonHostIdentifier(value).getIdentity().toSemanticType().getParameters();
            }
        } else {
            @Nonnull List<Block> elements = new ListWrapper(value).getElements();
            @Nonnull SemanticType[] parameters = new SemanticType[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                parameters[i] = new NonHostIdentifier(elements.get(i)).getIdentity().toSemanticType();
            }
            return parameters;
        }
        
        // semanticBase
        @Nullable Block value = Client.getAttributeUnwrapped(this, SEMANTIC_TYPE_SEMANTIC_BASE);
        if (value == null) return false;
        @Nonnull SemanticType superType = new NonHostIdentifier(value).getIdentity().toSemanticType();
        if (this.equals(superType)) return true;
        else return superType.isBasedOn(semanticType);
        
        */
        
        this.categories = new FreezableLinkedList<Category>().freeze();
        this.cachingPeriod = null;
        this.syntacticBase = DataWrapper.TYPE;
        this.parameters = new FreezableLinkedList<SemanticType>().freeze();
        this.semanticBase = null;
        setLoaded();
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param categories the categories for which this semantic type can be used as an attribute.
     * @param cachingPeriod the caching period of this semantic type when used as an attribute.
     * @param syntacticBase the syntactic type on which this semantic type is based.
     * @param parameters the generic parameters of the syntactic type.
     * 
     * @require !isLoaded() : "The type declaration may not yet have been loaded.";
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * 
     * @require categories.isFrozen() : "The categories have to be frozen.";
     * @require categories.doesNotContainNull() : "The categories may not contain null.";
     * @require categories.doesNotContainDuplicates(): "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
     * 
     * @require parameters.isFrozen() : "The parameters have to be frozen.";
     * @require parameters.doesNotContainNull() : "The parameters may not contain null.";
     * @require parameters.doesNotContainDuplicates(): "The parameters may not contain duplicates.";
     * 
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.size() > 0 || syntacticBase.getNumberOfParameters() == parameters.size() : "The number of required parameters has either to be variable or to match the given parameters.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull ReadonlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull SyntacticType syntacticBase, @Nonnull ReadonlyList<SemanticType> parameters) {
        assert !isLoaded() : "The type declaration may not yet have been loaded.";
        assert Database.isMainThread(): "This method may only be called in the main thread.";
        
        assert categories.isFrozen() : "The categories have to be frozen.";
        assert categories.doesNotContainNull() : "The categories may not contain null.";
        assert categories.doesNotContainDuplicates(): "The categories may not contain duplicates.";
        
        assert cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
        
        assert parameters.isFrozen() : "The parameters have to be frozen.";
        assert parameters.doesNotContainNull() : "The parameters may not contain null.";
        assert parameters.doesNotContainDuplicates(): "The parameters may not contain duplicates.";
        
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
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * 
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require !Identifier.isHost(identifier) : "The string may not denote a host identifier.";
     * 
     * @require new FreezableArray<Category>(categories).doesNotContainNull() : "The categories may not contain null.";
     * @require new FreezableArray<Category>(categories).doesNotContainDuplicates(): "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
     * 
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainNull() : "The parameters may not contain null.";
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainDuplicates(): "The parameters may not contain duplicates.";
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
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * 
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require !Identifier.isHost(identifier) : "The string may not denote a host identifier.";
     * 
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainNull() : "The parameters may not contain null.";
     * @require new FreezableArray<SemanticType>(parameters).doesNotContainDuplicates(): "The parameters may not contain duplicates.";
     * 
     * @require (categories.length == 0) == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * @require syntacticBase.getNumberOfParameters() == -1 && parameters.length > 0 || syntacticBase.getNumberOfParameters() == parameters.length : "The number of required parameters has either to be variable or to match the given parameters.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull SyntacticType syntacticBase, @Nonnull SemanticType... parameters) {
        return load(new Category[0], null, syntacticBase, parameters);
    }
    
    /**
     * Loads the type declaration from the given parameters.
     * 
     * @param categories the categories for which this semantic type can be used as an attribute.
     * @param cachingPeriod the caching period of this semantic type when used as an attribute.
     * @param semanticBase the semantic type on which this semantic type is based.
     * 
     * @require !isLoaded() : "The type declaration may not yet have been loaded.";
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * 
     * @require categories.isFrozen() : "The categories have to be frozen.";
     * @require categories.doesNotContainNull() : "The categories may not contain null.";
     * @require categories.doesNotContainDuplicates(): "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
     * 
     * @require semanticBase.isLoaded() : "The semantic base is already loaded.";
     * 
     * @require categories.isEmpty() == (cachingPeriod == null) : "The caching period is null if and only if the categories are empty.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull ReadonlyList<Category> categories, @Nullable Time cachingPeriod, @Nonnull SemanticType semanticBase) {
        assert !isLoaded() : "The type declaration may not yet have been loaded.";
        assert Database.isMainThread(): "This method may only be called in the main thread.";
        
        assert categories.isFrozen() : "The categories have to be frozen.";
        assert categories.doesNotContainNull() : "The categories may not contain null.";
        assert categories.doesNotContainDuplicates(): "The categories may not contain duplicates.";
        
        assert cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
        
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
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * 
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require !Identifier.isHost(identifier) : "The string may not denote a host identifier.";
     * 
     * @require new FreezableArray<Category>(categories).doesNotContainNull() : "The categories may not contain null.";
     * @require new FreezableArray<Category>(categories).doesNotContainDuplicates(): "The categories may not contain duplicates.";
     * 
     * @require cachingPeriod == null || cachingPeriod.isNonNegative() : "The caching period is null or non-negative.";
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
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * 
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require !Identifier.isHost(identifier) : "The string may not denote a host identifier.";
     * 
     * @require semanticBase.isLoaded() : "The semantic base is already loaded.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SemanticType load(@Nonnull SemanticType semanticBase) {
        return load(new Category[0], null, semanticBase);
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
     * @ensure return == null || return.isNonNegative() : "The caching period is null or non-negative.";
     */
    @Pure
    public @Nullable Time getCachingPeriod() {
        assert isLoaded() : "The type declaration is already loaded.";
        
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
     * Returns whether this semantic type can be used as an attribute.
     * 
     * @return whether this semantic type can be used as an attribute.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure isAttributeType() == (getCachingPeriod() != null) : "If (and only if) this semantic type can be used as an attribute, the caching period is not null.";
     */
    @Pure
    public boolean isAttributeType() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        assert categories != null;
        return !categories.isEmpty();
    }
    
    /**
     * Returns whether this semantic type can be used as an attribute for the given category.
     * 
     * @param category the category of interest.
     * 
     * @return whether this semantic type can be used as an attribute for the given category.
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
     * Returns whether this semantic type can be used to denote roles.
     * 
     * @return whether this semantic type can be used to denote roles.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     */
    @Pure
    public boolean isRoleType() {
        return isBasedOn(Context.FLAT);
    }
    
}
