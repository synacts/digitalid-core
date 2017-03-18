package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.recovery.Check;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.threading.Threading;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.elements.UniqueElements;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.math.relative.LessThanOrEqualTo;
import net.digitalid.utility.validation.annotations.method.Chainable;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.annotations.type.LoadedRecipient;
import net.digitalid.core.annotations.type.NonLoadedRecipient;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;

/**
 * This class models a semantic type.
 */
@Mutable
@GenerateSubclass
public abstract class SemanticType extends Type {
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    /**
     * Maps the semantic type with the given identifier.
     * <p>
     * This method can be called on any thread. However, the result should only be stored temporarily (because the transaction might be rolled back).
     * 
     * @throws UncheckedException instead of {@link DatabaseException} and {@link RecoveryException}.
     */
    @Pure
    @NonCommitting
    public static @Nonnull SemanticType mapWithoutPersistingResult(@Nonnull InternalNonHostIdentifier identifier) {
        final @Nonnull IdentifierResolver identifierResolver = IdentifierResolver.configuration.get();
        Log.verbose("Mapping the semantic type $.", identifier);
        try {
            @Nullable Identity identity = identifierResolver.load(identifier);
            if (identity == null) { identity = identifierResolver.map(Category.SEMANTIC_TYPE, identifier); }
            Check.that(identity instanceof SemanticType).orThrow("The mapped or loaded identity $ has to be a semantic type but was $.", identity.getAddress().getString(), identity.getClass().getSimpleName());
            return (SemanticType) identity;
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /**
     * Maps the semantic type of the given converter.
     * <p>
     * This method can be called on any thread. However, the result should only be stored temporarily (because the transaction might be rolled back).
     * 
     * @throws UncheckedException instead of {@link DatabaseException} and {@link RecoveryException}.
     */
    @Pure
    @NonCommitting
    public static @Nonnull SemanticType mapWithoutPersistingResult(@Nonnull Converter<?, ?> converter) {
        return mapWithoutPersistingResult(InternalNonHostIdentifier.of(converter));
    }
    
    /**
     * Maps the semantic type with the given identifier.
     * 
     * @throws UncheckedException instead of {@link DatabaseException} and {@link RecoveryException}.
     */
    @Pure
    @MainThread
    @NonCommitting
    public static @Nonnull SemanticType map(@Nonnull InternalNonHostIdentifier identifier) {
        Require.that(Threading.isMainThread()).orThrow("The method 'map' may only be called on the main thread.");
        
        return mapWithoutPersistingResult(identifier);
    }
    
    /**
     * Maps the semantic type with the given string, which has to be a valid internal non-host identifier.
     * 
     * @throws UncheckedException instead of {@link DatabaseException} and {@link RecoveryException}.
     */
    @Pure
    @MainThread
    @NonCommitting
    public static @Nonnull SemanticType map(@Nonnull String identifier) {
        return map(InternalNonHostIdentifier.with(identifier));
    }
    
    /**
     * Maps the semantic type of the given converter.
     * 
     * @throws UncheckedException instead of {@link DatabaseException} and {@link RecoveryException}.
     */
    @Pure
    @MainThread
    @NonCommitting
    public static @Nonnull SemanticType map(@Nonnull Converter<?, ?> converter) {
        return map(InternalNonHostIdentifier.of(converter));
    }
    
    /* -------------------------------------------------- Attributes -------------------------------------------------- */
    
    private @Nullable SemanticTypeAttributes attributes = null;
    
    /**
     * Returns the categories for which this semantic type can be used as an attribute.
     */
    @Pure
    @LoadedRecipient
    @SuppressWarnings("null")
    public @Nonnull @NonNullableElements @UniqueElements ImmutableList<Category> getCategories() {
        return attributes.getCategories();
    }
    
    /**
     * Returns the caching period of this semantic type when used as an attribute.
     */
    @Pure
    @LoadedRecipient
    @SuppressWarnings("null")
    public @Nonnull @NonNegative @LessThanOrEqualTo(/* Time.TROPICAL_YEAR: */ 31_556_925_190l) Time getCachingPeriod() {
        return attributes.getCachingPeriod();
    }
    
    /**
     * Returns the syntactic base of this semantic type.
     */
    @Pure
    @LoadedRecipient
    @SuppressWarnings("null")
    public @Nonnull @Loaded SyntacticType getSyntacticBase() {
        return attributes.getSyntacticBase();
    }
    
    /**
     * Returns the generic parameters of this semantic type.
     */
    @Pure
    @LoadedRecipient
    @SuppressWarnings("null")
    public @Nonnull @NonNullableElements @UniqueElements ImmutableList<SemanticType> getParameters() {
        return attributes.getParameters();
    }
    
    /**
     * Returns the semantic base of this semantic type.
     */
    @Pure
    @LoadedRecipient
    @SuppressWarnings("null")
    public @Nullable SemanticType getSemanticBase() {
        return attributes.getSemanticBase();
    }
    
    /* -------------------------------------------------- Loaded -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isLoaded() {
        return attributes != null;
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    @Impure
    @Override
    @Chainable
    @NonCommitting
    @NonLoadedRecipient
    @Nonnull SemanticType load() throws ExternalException {
        this.attributes = TypeLoader.configuration.get().load(this);
        return this;
    }
    
    /**
     * Loads the type declaration from the given attributes.
     */
    @Impure
    @Chainable
    @MainThread
    @NonLoadedRecipient
    public @Nonnull SemanticType load(@Nonnull SemanticTypeAttributes attributes) {
        this.attributes = attributes;
        return this;
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SEMANTIC_TYPE;
    }
    
    /* -------------------------------------------------- Attribute Type -------------------------------------------------- */
    
    /**
     * Returns whether this semantic type can be used to denote an attribute.
     */
    @Pure
    @LoadedRecipient
    public boolean isAttributeType() {
        return !getCategories().isEmpty();
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute.
     * 
     * @throws RecoveryException if this is not the case.
     */
    @Pure
    @Chainable
    @LoadedRecipient
    public @Nonnull SemanticType checkIsAttributeType() throws RecoveryException {
        if (!isAttributeType()) { throw RecoveryExceptionBuilder.withMessage(Quotes.inSingle(getAddress()) + " has to be an attribute type.").build(); }
        return this;
    }
    
    /**
     * Returns whether this semantic type can be used to denote an attribute for the given category.
     */
    @Pure
    @LoadedRecipient
    public boolean isAttributeFor(@Nonnull Category category) {
        return getCategories().contains(category);
    }
    
    /**
     * Checks that this semantic type can be used to denote an attribute for the given category.
     * 
     * @throws RecoveryException if this is not the case.
     */
    @Pure
    @Chainable
    @LoadedRecipient
    public @Nonnull @Loaded /* @AttributeType */ SemanticType checkIsAttributeFor(@Nonnull Category category) throws RecoveryException {
        if (!isAttributeType()) { throw RecoveryExceptionBuilder.withMessage(Quotes.inSingle(getAddress()) + " has to be an attribute type for the category " + Quotes.inSingle(category) + ".").build(); }
        return this;
    }
    
    /* -------------------------------------------------- Based On -------------------------------------------------- */
    
    /**
     * Returns whether this semantic type is (indirectly) based on the given syntactic type.
     */
    @Pure
    @LoadedRecipient
    public boolean isBasedOn(@Nonnull SyntacticType syntacticType) {
        return getSyntacticBase().equals(syntacticType);
    }
    
    /**
     * Returns whether this semantic type is (indirectly) based on the given semantic type.
     * This relation is reflexive, transitive and antisymmetric.
     */
    @Pure
    @LoadedRecipient
    public boolean isBasedOn(@Nonnull SemanticType semanticType) {
        final @Nullable SemanticType semanticBase = getSemanticBase();
        return equals(semanticType) || semanticBase != null && semanticBase.isBasedOn(semanticType);
    }
    
    /* -------------------------------------------------- Role Type -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType PERSON = SemanticType.map("person@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.STRING).build());
    
    /**
     * Stores the semantic type {@code flat.context@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType FLAT_CONTEXT = SemanticType.map("flat.context@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.LIST).withParameters(ImmutableList.withElements(PERSON)).withCategories(Category.PERSONS).withCachingPeriod(Time.HALF_DAY).build());
    
    /**
     * Returns whether this semantic type can be used to denote a role.
     */
    @Pure
    @LoadedRecipient
    public boolean isRoleType() {
        return isBasedOn(FLAT_CONTEXT);
    }
    
    /**
     * Checks that this semantic type can be used to denote a role.
     * 
     * @throws RecoveryException if this is not the case.
     */
    @Pure
    @Chainable
    @LoadedRecipient
    public @Nonnull @Loaded /* @RoleType */ SemanticType checkIsRoleType() throws RecoveryException {
        if (!isRoleType()) { throw RecoveryExceptionBuilder.withMessage(Quotes.inSingle(getAddress()) + " has to be a role type.").build(); }
        return this;
    }
    
}
