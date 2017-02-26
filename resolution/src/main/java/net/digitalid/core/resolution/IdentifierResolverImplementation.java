package net.digitalid.core.resolution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.converters.Integer64Converter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.unit.Unit;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identifier.EmailIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.IdentifierConverter;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.MobileIdentifier;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.resolution.handlers.IdentityQuery;
import net.digitalid.core.resolution.handlers.IdentityQueryBuilder;
import net.digitalid.core.resolution.handlers.IdentityReply;
import net.digitalid.core.resolution.handlers.IdentityReplyConverter;
import net.digitalid.core.resolution.tables.IdentifierEntry;
import net.digitalid.core.resolution.tables.IdentifierEntryBuilder;
import net.digitalid.core.resolution.tables.IdentifierEntryConverter;
import net.digitalid.core.resolution.tables.IdentityEntry;
import net.digitalid.core.resolution.tables.IdentityEntryBuilder;
import net.digitalid.core.resolution.tables.IdentityEntryConverter;

/**
 * This class implements the {@link IdentifierResolver}.
 */
@Mutable
@GenerateSubclass
public abstract class IdentifierResolverImplementation extends IdentifierResolver {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static @Nonnull IdentifierResolverImplementation INSTANCE = new IdentifierResolverImplementationSubclass();
    
    /* -------------------------------------------------- Map Caches -------------------------------------------------- */
    
    /**
     * Maps numbers onto identities by caching the corresponding entries from the database.
     */
    private final @Nonnull Map<@Nonnull Long, @Nonnull Identity> keys = new ConcurrentHashMap<>();
    
    /**
     * Maps identifiers onto identities by caching the corresponding entries from the database.
     */
    private final @Nonnull Map<@Nonnull Identifier, @Nonnull Identity> identifiers = new ConcurrentHashMap<>();
    
    /**
     * Clears the local maps of the mapper.
     */
    @Impure
    public void clearLocalMaps() {
        keys.clear();
        identifiers.clear();
    }
    
    /**
     * Removes the given identity from the local maps.
     */
    @Impure
    public void unmap(@Nonnull Identity identity) {
        keys.remove(identity.getKey());
        identifiers.remove(identity.getAddress());
        Log.debugging("The identity of " + identity.getAddress() + " was unmapped.");
    }
    
    /* -------------------------------------------------- Key Resolution -------------------------------------------------- */
    
    @Override
    @PureWithSideEffects
    public @Nonnull Identity load(long key) throws DatabaseException, RecoveryException {
        @Nullable Identity identity = keys.get(key);
        if (identity == null) {
            final @Nonnull IdentityEntry entry = SQL.selectOne(IdentityEntryConverter.INSTANCE, null, Integer64Converter.INSTANCE, key, "key", Unit.DEFAULT);
            identity = createIdentity(entry.getCategory(), entry.getKey(), entry.getAddress());
            identifiers.put(entry.getAddress(), identity);
            keys.put(key, identity);
        }
        return identity;
    }
    
    /* -------------------------------------------------- Identity Mapping -------------------------------------------------- */
    
    /**
     * Maps the given address to a new key. Make sure that the identifier is not already mapped!
     */
    @PureWithSideEffects
    @TODO(task = "Instead of a random key, we could/should use an auto-incrementing column in the database.", date = "2017-02-26", author = Author.KASPAR_ETTER)
    private @Nonnull Identity map(@Nonnull Identifier address, @Nonnull Category category) throws DatabaseException {
        final long key = ThreadLocalRandom.current().nextLong();
        
//        if (address.getString().equals("person@core.digitalid.net")) {
//            new Exception().printStackTrace();
//        }
        
        final @Nonnull IdentityEntry identityEntry = IdentityEntryBuilder.withKey(key).withCategory(category).withAddress(address).build();
        final @Nonnull IdentifierEntry identifierEntry = IdentifierEntryBuilder.withIdentifier(address).withKey(key).build();
        SQL.insert(IdentityEntryConverter.INSTANCE, identityEntry, Unit.DEFAULT);
        SQL.insert(IdentifierEntryConverter.INSTANCE, identifierEntry, Unit.DEFAULT);
        
        final @Nonnull Identity identity = createIdentity(category, key, address);
        identifiers.put(address, identity);
        keys.put(key, identity);
        return identity;
    }
    
    /* -------------------------------------------------- Identifier Resolution -------------------------------------------------- */
    
    /**
     * Loads and returns the identity with the given identifier from the database.
     */
    @PureWithSideEffects
    public @Nullable Identity load(@Nonnull Identifier identifier) throws DatabaseException, RecoveryException {
        final @Nullable IdentifierEntry entry = SQL.selectFirst(IdentifierEntryConverter.INSTANCE, null, IdentifierConverter.INSTANCE, identifier, "identifier_", Unit.DEFAULT);
        return entry != null ? load(entry.getKey()) : null;
    }
    
    @Override
    @PureWithSideEffects
    public @Nonnull Identity resolve(@Nonnull Identifier identifier) throws ExternalException {
        @Nullable Identity identity = identifiers.get(identifier);
        if (identity == null) {
            identity = load(identifier);
            if (identity == null) {
                if (identifier instanceof HostIdentifier) {
                    // TODO: HostIdentifiers have to be handled differently (because the response signature cannot be verified immediately).
                    identity = map(identifier, Category.HOST); // TODO: This line is only temporary.
                } else if (identifier instanceof InternalIdentifier) {
                    final @Nonnull IdentityQuery query = IdentityQueryBuilder.withProvidedSubject((InternalIdentifier) identifier).build();
                    final @Nonnull IdentityReply reply = query.send(IdentityReplyConverter.INSTANCE);
                    identity = map(identifier, reply.getCategory());
                } else if (identifier instanceof EmailIdentifier) {
                    identity = map(identifier, Category.EMAIL_PERSON);
                } else if (identifier instanceof MobileIdentifier) {
                    identity = map(identifier, Category.MOBILE_PERSON);
                } else {
                    throw CaseExceptionBuilder.withVariable("identifier").withValue(identifier).build();
                }
            }
        }
        return identity;
    }
    
    /* -------------------------------------------------- Syntactic Type Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @MainThread
    protected @Nonnull @NonLoaded SyntacticType mapSyntacticType(@Nonnull InternalNonHostIdentifier identifier) {
        try {
            return map(identifier, Category.SYNTACTIC_TYPE).castTo(SyntacticType.class);
        } catch (@Nonnull DatabaseException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /* -------------------------------------------------- Semantic Type Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @MainThread
    protected @Nonnull @NonLoaded SemanticType mapSemanticType(@Nonnull InternalNonHostIdentifier identifier) {
        try {
            return map(identifier, Category.SEMANTIC_TYPE).castTo(SemanticType.class);
        } catch (@Nonnull DatabaseException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /* -------------------------------------------------- Injection -------------------------------------------------- */
    
    /**
     * Initializes the identifier resolver.
     */
    @PureWithSideEffects
    @Initialize(target = IdentifierResolver.class, dependencies = Database.class)
    public static void initializeIdentifierResolver() throws DatabaseException {
        SQL.createTable(IdentityEntryConverter.INSTANCE, Unit.DEFAULT); // TODO: GeneralUnit.INSTANCE);
        SQL.createTable(IdentifierEntryConverter.INSTANCE, Unit.DEFAULT); // TODO: GeneralUnit.INSTANCE);
        IdentifierResolver.configuration.set(INSTANCE);
    }
    
}
