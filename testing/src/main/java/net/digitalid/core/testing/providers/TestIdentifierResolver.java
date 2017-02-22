package net.digitalid.core.testing.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SyntacticType;

/**
 * This class implements the {@link IdentifierResolver} for unit tests.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public class TestIdentifierResolver extends IdentifierResolver {
    
    /* -------------------------------------------------- Key Resolution -------------------------------------------------- */
    
    private static final @Nonnull Map<@Nonnull Long, @Nonnull Identity> keys = new HashMap<>();
    
    @Pure
    @Override
    public @Nonnull Identity getIdentity(long key) {
        @Nullable Identity identity = keys.get(key);
        if (identity == null) {
            identity = createSemanticType(key, InternalNonHostIdentifier.with("todo@digitalid.net"));
            keys.put(key, identity);
        }
        return identity;
    }
    
    /* -------------------------------------------------- Syntactic Type Mapping -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Syntactic Type Mapping -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Identifier Resolution -------------------------------------------------- */
    
    private static final @Nonnull Map<@Nonnull Identifier, @Nonnull Identity> identifiers = new HashMap<>();
    
    @Pure
    @Override
    public @Nonnull Identity getIdentity(@Nonnull Identifier identifier) {
        @Nullable Identity identity = identifiers.get(identifier);
        if (identity == null) {
            if (identifier instanceof HostIdentifier) {
                identity = createHostIdentity(ThreadLocalRandom.current().nextInt(), (HostIdentifier) identifier);
            } else if (identifier instanceof InternalNonHostIdentifier) {
                identity = mapSemanticType((InternalNonHostIdentifier) identifier);
            } else {
                throw new UnsupportedOperationException("The identifier resolver does not support '" + identifier.getClass() + "' yet.");
            }
            identifiers.put(identifier, identity);
        }
        return identity;
    }
    
}
