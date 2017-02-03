package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.errors.InitializationError;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identifier.EmailIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.MobileIdentifier;

/**
 * The identifier resolver resolves an identifier into the corresponding identity.
 * This type is a class instead of an interface to expose only package-visible executables through protected methods.
 */
@Immutable
public abstract class IdentifierResolver {
    
    /* -------------------------------------------------- Resolution -------------------------------------------------- */
    
    /**
     * Returns the identity of the given key.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull Identity getIdentity(long key) throws DatabaseException, RecoveryException;
    
    /**
     * Returns the identity of the given identifier.
     * The identity is also established if required.
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull Identity getIdentity(@Nonnull Identifier identifier) throws ExternalException;
    
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
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the identity with the given key.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Identity load(long key) throws DatabaseException, RecoveryException {
        return configuration.get().getIdentity(key);
    }
    
    /**
     * Resolves the given identifier into an identity.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Identity resolve(@Nonnull Identifier identifier) throws ExternalException {
        return configuration.get().getIdentity(identifier);
    }
    
    /* -------------------------------------------------- Identity Creation -------------------------------------------------- */
    
    @Pure
    protected static @Nonnull HostIdentity createHostIdentity(long key, @Nonnull HostIdentifier address) {
        return new HostIdentitySubclass(key, address);
    }
    
    @Pure
    protected static @Nonnull SemanticType createSemanticType(long key, @Nonnull InternalNonHostIdentifier address) {
        return new SemanticTypeSubclass(key, address);
    }
    
    @Pure
    protected static @Nonnull SyntacticType createSyntacticType(long key, @Nonnull InternalNonHostIdentifier address) {
        return new SyntacticTypeSubclass(key, address);
    }
    
    @Pure
    protected static @Nonnull NaturalPerson createNaturalPerson(long key, @Nonnull InternalNonHostIdentifier address) {
        return new NaturalPersonSubclass(key, address);
    }
    
    @Pure
    protected static @Nonnull ArtificialPerson createArtificialPerson(long key, @Nonnull InternalNonHostIdentifier address) {
        return new ArtificialPersonSubclass(key, address);
    }
    
    @Pure
    protected static @Nonnull EmailPerson createEmailPerson(long key, @Nonnull EmailIdentifier address) {
        return new EmailPersonSubclass(key, address, Category.EMAIL_PERSON);
    }
    
    @Pure
    protected static @Nonnull MobilePerson createMobilePerson(long key, @Nonnull MobileIdentifier address) {
        return new MobilePersonSubclass(key, address, Category.MOBILE_PERSON);
    }
    
    /* -------------------------------------------------- Type Loading -------------------------------------------------- */
    
    @Pure
    protected static void load(@Nonnull @NonLoaded Type type) throws ExternalException {
        type.load();
    }
    
    /* -------------------------------------------------- Identity Relocation -------------------------------------------------- */
    
    @Pure
    protected static void setAddress(@NonCaptured @Modified @Nonnull RelocatableIdentity identity, @Nonnull InternalNonHostIdentifier address) {
        identity.setAddress(address);
    }
    
    /* -------------------------------------------------- Identity Merging -------------------------------------------------- */
    
    @Pure
    protected static void setKey(@NonCaptured @Modified @Nonnull Person person, long key) {
        person.setKey(key);
    }
    
    /* -------------------------------------------------- Category Change -------------------------------------------------- */
    
    @Pure
    protected static void setCategory(@NonCaptured @Modified @Nonnull ExternalPerson person, @Nonnull Category category) {
        person.setCategory(category);
    }
    
}
