package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.errors.InitializationError;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.method.Ensures;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identifier.EmailIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.MobileIdentifier;
import net.digitalid.core.identification.identifier.PseudoIdentifierResolver;

/**
 * The identifier resolver resolves an identifier into the corresponding identity.
 * This type is a class instead of an interface to expose only package-visible executables through protected methods.
 */
@Stateless
public abstract class IdentifierResolver {
    
    /* -------------------------------------------------- Resolution -------------------------------------------------- */
    
    /**
     * Loads the identity with the given key.
     */
    @Pure
    @NonCommitting
    protected abstract @Nonnull Identity load(long key) throws DatabaseException, RecoveryException;
    
    /**
     * Resolves the given identifier into an identity.
     */
    @Pure
    @NonCommitting
    protected abstract @Nonnull Identity resolve(@Nonnull Identifier identifier) throws ExternalException;
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    /**
     * Maps the given identifier and returns a syntactic type with that address.
     * All exceptions are suppressed with an {@link InitializationError}.
     */
    @Pure
    @MainThread
    protected abstract @Nonnull @NonLoaded SyntacticType mapSyntacticType(@Nonnull InternalNonHostIdentifier identifier);
    
    /**
     * Maps the given identifier and returns a semantic type with that address.
     * All exceptions are suppressed with an {@link InitializationError}.
     */
    @Pure
    @MainThread
    protected abstract @Nonnull @NonLoaded SemanticType mapSemanticType(@Nonnull InternalNonHostIdentifier identifier);
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the identifier resolver, which has to be provided by another package.
     */
    public static final @Nonnull Configuration<IdentifierResolver> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Expose Access -------------------------------------------------- */
    
    /**
     * This method allows the {@link PseudoIdentifierResolver} to access the identifier resolution of the configured provider.
     */
    @Pure
    @NonCommitting
    protected static @Nonnull Identity resolveWithProvider(@Nonnull Identifier identifier) throws ExternalException {
        return configuration.get().resolve(identifier);
    }
    
    /* -------------------------------------------------- Identity Creation -------------------------------------------------- */
    
    /**
     * Returns a new host identity with the given key and address.
     */
    @Pure
    protected @Nonnull HostIdentity createHostIdentity(long key, @Nonnull HostIdentifier address) {
        return new HostIdentitySubclass(key, address);
    }
    
    /**
     * Returns a new syntactic type with the given key and address.
     */
    @Pure
    protected @Nonnull @NonLoaded SyntacticType createSyntacticType(long key, @Nonnull InternalNonHostIdentifier address) {
        return new SyntacticTypeSubclass(key, address);
    }
    
    /**
     * Returns a new semantic type with the given key and address.
     */
    @Pure
    protected @Nonnull @NonLoaded SemanticType createSemanticType(long key, @Nonnull InternalNonHostIdentifier address) {
        return new SemanticTypeSubclass(key, address);
    }
    
    /**
     * Returns a new natural person with the given key and address.
     */
    @Pure
    protected @Nonnull NaturalPerson createNaturalPerson(long key, @Nonnull InternalNonHostIdentifier address) {
        return new NaturalPersonSubclass(key, address);
    }
    
    /**
     * Returns a new artificial person with the given key and address.
     */
    @Pure
    protected @Nonnull ArtificialPerson createArtificialPerson(long key, @Nonnull InternalNonHostIdentifier address) {
        return new ArtificialPersonSubclass(key, address);
    }
    
    /**
     * Returns a new email person with the given key and address.
     */
    @Pure
    protected @Nonnull EmailPerson createEmailPerson(long key, @Nonnull EmailIdentifier address) {
        return new EmailPersonSubclass(key, address, Category.EMAIL_PERSON);
    }
    
    /**
     * Returns a new mobile person with the given key and address.
     */
    @Pure
    protected @Nonnull MobilePerson createMobilePerson(long key, @Nonnull MobileIdentifier address) {
        return new MobilePersonSubclass(key, address, Category.MOBILE_PERSON);
    }
    
    /**
     * Returns a new identity of the given category with the given number and address (with types not yet loaded).
     */
    @Pure
    @NonCommitting
    @Ensures(condition = "result.getCategory().equals(category)", message = "The category of the returned identity equals the given category.")
    protected @Nonnull Identity createIdentity(@Nonnull Category category, long number, @Nonnull Identifier address) {
        switch (category) {
            case HOST: return createHostIdentity(number, address.castTo(HostIdentifier.class));
            case SYNTACTIC_TYPE: return createSyntacticType(number, address.castTo(InternalNonHostIdentifier.class));
            case SEMANTIC_TYPE: return createSemanticType(number, address.castTo(InternalNonHostIdentifier.class));
            case NATURAL_PERSON: return createNaturalPerson(number, address.castTo(InternalNonHostIdentifier.class));
            case ARTIFICIAL_PERSON: return createArtificialPerson(number, address.castTo(InternalNonHostIdentifier.class));
            case EMAIL_PERSON: return createEmailPerson(number, address.castTo(EmailIdentifier.class));
            case MOBILE_PERSON: return createMobilePerson(number, address.castTo(MobileIdentifier.class));
            default: throw CaseExceptionBuilder.withVariable("category").withValue(category).build();
        }
    }
    
    /* -------------------------------------------------- Type Loading -------------------------------------------------- */
    
    @Pure
    protected void load(@Nonnull @NonLoaded Type type) throws ExternalException {
        type.load();
    }
    
    /* -------------------------------------------------- Identity Relocation -------------------------------------------------- */
    
    @Pure
    protected void setAddress(@NonCaptured @Modified @Nonnull RelocatableIdentity identity, @Nonnull InternalNonHostIdentifier address) {
        identity.setAddress(address);
    }
    
    /* -------------------------------------------------- Identity Merging -------------------------------------------------- */
    
    @Pure
    protected void setKey(@NonCaptured @Modified @Nonnull Person person, long key) {
        person.setKey(key);
    }
    
    /* -------------------------------------------------- Category Change -------------------------------------------------- */
    
    @Pure
    protected void setCategory(@NonCaptured @Modified @Nonnull ExternalPerson person, @Nonnull Category category) {
        person.setCategory(category);
    }
    
}
