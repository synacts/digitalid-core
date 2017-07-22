package net.digitalid.core.client.role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Provide;
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
@GenerateTableConverter
public abstract class RoleEntry extends RootClass {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @PrimaryKey
    // TODO: @AutoIncrement
    public abstract long getKey();
    
    @Pure
    public abstract @Nonnull Client getClient();
    
    @Pure
    @Provide("client")
    public abstract @Nonnull RoleArguments getArguments();
    
    /* -------------------------------------------------- Import -------------------------------------------------- */
    
    @Pure
    public static @Nonnull RoleEntry from(@Nonnull Role role) {
        final RoleArgumentsBuilder.@Nonnull InnerRoleArgumentsBuilder builder = RoleArgumentsBuilder.withClient(role.getUnit()).withIssuer(role.getIssuer()).withAgentKey(role.getAgentKey());
        if (role instanceof NonNativeRole) {
            final @Nonnull NonNativeRole nonNativeRole = (NonNativeRole) role;
            builder.withRelation(nonNativeRole.getRelation()).withRecipient(nonNativeRole.getRecipient().getKey());
        };
        final @Nonnull RoleArguments arguments = builder.build();
        return RoleEntryBuilder.withKey(role.getKey()).withClient(role.getUnit()).withArguments(arguments).build();
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
            return new NonNativeRoleSubclass(getKey(), client, issuer, agentKey, relation, Role.with(client, recipient));
        } else {
            return new NativeRoleSubclass(getKey(), client, issuer, agentKey);
        }
    }
    
}
