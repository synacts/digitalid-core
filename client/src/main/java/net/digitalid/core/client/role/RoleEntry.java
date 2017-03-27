package net.digitalid.core.client.role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Provide;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.Client;
import net.digitalid.core.identification.annotations.RoleType;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This type models an entry in the role table.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class RoleEntry extends RootClass {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Provided
    public abstract @Nonnull Client getClient();
    
    @Pure
    @PrimaryKey
    // TODO: @AutoIncrement
    public abstract long getKey();
    
    @Pure
    @Provide("client")
    public abstract @Nonnull RoleArguments getArguments();
    
    /* -------------------------------------------------- Import -------------------------------------------------- */
    
    @Pure
    public static @Nonnull RoleEntry from(@Nonnull Role role) {
        return null; // TODO
    }
    
    /* -------------------------------------------------- Export -------------------------------------------------- */
    
    @Pure
    public @Nonnull Role toRole() throws DatabaseException, RecoveryException {
        final @Nonnull RoleArguments arguments = getArguments();
        final @Nonnull Client client = arguments.getClient();
        final @Nonnull InternalNonHostIdentity issuer = arguments.getIssuer();
        final @Nullable @RoleType SemanticType relation = arguments.getRelation();
        final @Nullable Long recipient = arguments.getRecipient();
        final long agentKey = arguments.getAgentKey();
        
        if (relation != null && recipient != null) {
            return new NonNativeRoleSubclass(client, getKey(), issuer, agentKey, relation, Role.with(client, recipient));
        } else {
            return new NativeRoleSubclass(client, getKey(), issuer, agentKey);
        }
    }
    
}
