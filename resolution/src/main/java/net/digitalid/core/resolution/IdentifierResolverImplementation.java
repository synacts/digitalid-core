package net.digitalid.core.resolution;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.converters.Integer64Converter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identifier.EmailIdentifier;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.IdentifierConverter;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.MobileIdentifier;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.Type;
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
import net.digitalid.core.unit.GeneralUnit;

/**
 * This class implements the {@link IdentifierResolver}.
 */
@Immutable
@GenerateSubclass
public abstract class IdentifierResolverImplementation extends IdentifierResolver {
    
    /* -------------------------------------------------- Injection -------------------------------------------------- */
    
    /**
     * Initializes the identifier resolver.
     */
    @PureWithSideEffects
    @Initialize(target = IdentifierResolver.class, dependencies = GeneralUnit.class)
    public static void initializeIdentifierResolver() throws DatabaseException {
        SQL.createTable(IdentityEntryConverter.INSTANCE, GeneralUnit.INSTANCE);
        SQL.createTable(IdentifierEntryConverter.INSTANCE, GeneralUnit.INSTANCE);
        IdentifierResolver.configuration.set(new IdentifierResolverImplementationSubclass());
    }
    
    /* -------------------------------------------------- Mapper -------------------------------------------------- */
    
    private final @Nonnull Mapper mapper = new MapperSubclass();
    
    /* -------------------------------------------------- Key Loading -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    public @Nonnull Identity load(long key) throws DatabaseException, RecoveryException {
        @Nullable Identity identity = mapper.getIdentity(key);
        if (identity == null) {
            final @Nonnull IdentityEntry entry = SQL.selectOne(IdentityEntryConverter.INSTANCE, null, Integer64Converter.INSTANCE, key, "key", GeneralUnit.INSTANCE);
            identity = createIdentity(entry.getCategory(), entry.getKey(), entry.getAddress());
            mapper.mapAfterCommit(identity);
        }
        return identity;
    }
    
    /* -------------------------------------------------- Identifier Loading -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    public @Nullable Identity load(@Nonnull Identifier identifier) throws DatabaseException, RecoveryException {
        @Nullable Identity identity = mapper.getIdentity(identifier);
        if (identity == null) {
            final @Nullable IdentifierEntry entry = SQL.selectFirst(IdentifierEntryConverter.INSTANCE, null, IdentifierConverter.INSTANCE, identifier, "identifier", GeneralUnit.INSTANCE);
            if (entry != null) { identity = load(entry.getKey()); }
            if (identity != null) { Log.verbose("Found the identifier $ in the database.", identifier.getString()); }
        } else { Log.verbose("Found the identifier $ in the hash map.", identifier.getString()); }
        return identity;
    }
    
    /* -------------------------------------------------- Identfier Mapping -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    @TODO(task = "Instead of a random key, we could/should use an auto-incrementing column in the database.", date = "2017-02-26", author = Author.KASPAR_ETTER)
    public @Nonnull Identity map(@Nonnull Category category, @Nonnull Identifier address) throws DatabaseException {
        final long key = ThreadLocalRandom.current().nextLong();
        
        final @Nonnull IdentityEntry identityEntry = IdentityEntryBuilder.withKey(key).withCategory(category).withAddress(address).build();
        final @Nonnull IdentifierEntry identifierEntry = IdentifierEntryBuilder.withIdentifier(address).withKey(key).build();
        SQL.insertOrAbort(IdentityEntryConverter.INSTANCE, identityEntry, GeneralUnit.INSTANCE);
        SQL.insertOrAbort(IdentifierEntryConverter.INSTANCE, identifierEntry, GeneralUnit.INSTANCE);
        
        final @Nonnull Identity identity = createIdentity(category, key, address);
        mapper.mapAfterCommit(identity);
        return identity;
    }
    
    /* -------------------------------------------------- Identifier Resolution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    public @Nonnull Identity resolve(@Nonnull Identifier identifier) throws ExternalException {
        Log.verbose("Resolving the identifier $.", identifier.getString());
        @Nullable Identity identity = load(identifier);
        if (identity == null) {
            if (identifier instanceof HostIdentifier) {
                // TODO: HostIdentifiers have to be handled differently (because the response signature cannot be verified immediately).
                identity = map(Category.HOST, identifier); // TODO: This line is only temporary.
            } else if (identifier instanceof InternalNonHostIdentifier) {
                final @Nonnull InternalNonHostIdentifier internalNonHostIdentifier = (InternalNonHostIdentifier) identifier;
                final @Nonnull HostIdentifier hostIdentifier = internalNonHostIdentifier.getHostIdentifier();
                if (Host.exists(hostIdentifier)) {
                    Log.verbose("The identifier $ is hosted on this server and is therefore not queried.", identifier.getString());
                    throw RequestExceptionBuilder.withCode(RequestErrorCode.IDENTITY).withMessage("The identifier '" + identifier.getString() + "' is hosted on this server and is therefore not queried.").build();
                } else if (hostIdentifier.equals(HostIdentifier.DIGITALID)) {
                    Log.verbose("The identifier $ is mapped as a semantic type without querying.", identifier.getString());
                    identity = map(Category.SEMANTIC_TYPE, identifier);
                } else {
                    Log.verbose("Querying the identifier $.", identifier.getString());
                    final @Nonnull IdentityQuery query = IdentityQueryBuilder.withProvidedSubject(internalNonHostIdentifier).build();
                    final @Nonnull IdentityReply reply = query.send(IdentityReplyConverter.INSTANCE);
                    identity = map(reply.getCategory(), identifier);
                }
            } else if (identifier instanceof EmailIdentifier) {
                identity = map(Category.EMAIL_PERSON, identifier);
            } else if (identifier instanceof MobileIdentifier) {
                identity = map(Category.MOBILE_PERSON, identifier);
            } else {
                throw CaseExceptionBuilder.withVariable("identifier").withValue(identifier).build();
            }
        }
        if (identity instanceof Type) { ((Type) identity).ensureLoaded(); }
        return identity;
    }
    
}
