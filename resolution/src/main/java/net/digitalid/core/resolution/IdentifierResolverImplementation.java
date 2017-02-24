package net.digitalid.core.resolution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.unit.Unit;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.resolution.tables.IdentifierEntryConverter;
import net.digitalid.core.resolution.tables.IdentityEntryConverter;

/**
 * This class implements the {@link IdentifierResolver}.
 */
@Stateless
public class IdentifierResolverImplementation extends IdentifierResolver {
    
    /* -------------------------------------------------- Implementation -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Identity getIdentity(long key) throws DatabaseException, RecoveryException {
        return createSemanticType(key, InternalNonHostIdentifier.with("todo@digitalid.net"));
    }
    
    @Pure
    @Override
    public @Nonnull Identity getIdentity(@Nonnull Identifier identifier) throws ExternalException {
        if (identifier instanceof HostIdentifier) {
            return createHostIdentity(1L, (HostIdentifier) identifier);
        } else if (identifier instanceof InternalNonHostIdentifier) {
            final @Nonnull InternalNonHostIdentifier internalIdentifier = (InternalNonHostIdentifier) identifier;
            if (semanticTypes.containsKey(internalIdentifier)) {
                return semanticTypes.get(internalIdentifier);
            } else {
                return createSemanticType(2L, internalIdentifier);
            }
        } else {
            throw new UnsupportedOperationException("The identifier resolver does not support '" + identifier.getClass() + "' yet.");
        }
    }
    
    private static final @Nonnull Map<@Nonnull InternalNonHostIdentifier, @Nonnull SyntacticType> syntacticTypes = new HashMap<>();
    
    @Pure
    @Override
    @MainThread
    protected @Nonnull @NonLoaded SyntacticType mapSyntacticType(@Nonnull InternalNonHostIdentifier identifier) {
        @Nullable SyntacticType type = syntacticTypes.get(identifier);
        if (type == null) {
            type = createSyntacticType(ThreadLocalRandom.current().nextInt(), identifier);
            syntacticTypes.put(identifier, type);
        }
        return type;
    }
    
    private static final @Nonnull Map<@Nonnull InternalNonHostIdentifier, @Nonnull SemanticType> semanticTypes = new HashMap<>();
    
    @Pure
    @Override
    @MainThread
    protected @Nonnull @NonLoaded SemanticType mapSemanticType(@Nonnull InternalNonHostIdentifier identifier) {
        @Nullable SemanticType type = semanticTypes.get(identifier);
        if (type == null) {
            type = createSemanticType(ThreadLocalRandom.current().nextInt(), identifier);
            semanticTypes.put(identifier, type);
        }
        return type;
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
        IdentifierResolver.configuration.set(new IdentifierResolverImplementation());
    }
    
}
