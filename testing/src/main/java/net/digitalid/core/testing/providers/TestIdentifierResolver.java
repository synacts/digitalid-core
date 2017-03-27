package net.digitalid.core.testing.providers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.exceptions.DatabaseExceptionBuilder;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;

/**
 * This class implements the {@link IdentifierResolver} for unit tests.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public class TestIdentifierResolver extends IdentifierResolver {
    
    /* -------------------------------------------------- Key Loading -------------------------------------------------- */
    
    private static final @Nonnull Map<@Nonnull Long, @Nonnull Identity> keys = new HashMap<>();
    
    @Pure
    @Override
    public @Nonnull Identity load(long key) throws DatabaseException {
        @Nullable Identity identity = keys.get(key);
        if (identity == null) { throw DatabaseExceptionBuilder.withCause(new SQLException("There exists no identity with the key " + key + ".")).build(); }
        return identity;
    }
    
    /* -------------------------------------------------- Identifier Loading -------------------------------------------------- */
    
    private static final @Nonnull Map<@Nonnull Identifier, @Nonnull Identity> identifiers = new HashMap<>();
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    public @Nullable Identity load(@Nonnull Identifier identifier) throws DatabaseException {
        return identifiers.get(identifier);
    }
    
    /* -------------------------------------------------- Identifier Mapping -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    public @Nonnull Identity map(@Nonnull Category category, @Nonnull Identifier address) throws DatabaseException {
        final long key = ThreadLocalRandom.current().nextLong();
        final @Nonnull Identity identity = createIdentity(category, key, address);
        identifiers.put(address, identity);
        keys.put(key, identity);
        return identity;
    }
    
    /* -------------------------------------------------- Identifier Resolution -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Identity resolve(@Nonnull Identifier identifier) throws DatabaseException {
        @Nullable Identity identity = load(identifier);
        if (identity == null) {
            if (identifier instanceof HostIdentifier) {
                identity = map(Category.HOST, identifier);
            } else if (identifier instanceof InternalNonHostIdentifier) {
                identity = map(Category.SEMANTIC_TYPE, identifier);
            } else {
                throw new UnsupportedOperationException("The test identifier resolver does not support '" + identifier.getClass() + "' yet.");
            }
        }
        return identity;
    }
    
}
