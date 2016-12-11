package net.digitalid.core.client;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.property.value.ReadOnlyVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValuePropertyBuilder;
import net.digitalid.utility.rootclass.RootClassWithException;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.set.WritablePersistentSimpleSetProperty;
import net.digitalid.database.subject.site.Site;
import net.digitalid.database.subject.annotations.GeneratePersistentProperty;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.client.role.NativeRole;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.commitment.CommitmentBuilder;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;

/**
 * A client is configured with an identifier and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 */
@Immutable
// TODO: @GenerateSubclass
public abstract class Client extends RootClassWithException<ExternalException> implements Site {
    
    /* -------------------------------------------------- Stop -------------------------------------------------- */
    
    // TODO: Move the following code somewhere else.
    
//    /**
//     * Stops the background threads of the client without shutting down.
//     */
//    public static void stop() {
//        Database.stopPurging();
//        Synchronizer.shutDown();
//        ResponseAudit.shutDown();
//    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    // TODO: Remove the following code once its logic is reflected somewhere else.
    
//    @Committing
//    public Client() throws IOException, ExternalException {
//        // The role table needs to be created in advance.
//        RoleModule.createTable(this);
//        CoreService.SERVICE.createTables(this);
//        SynchronizerModule.load(this);
//        Database.commit();
//    }
    
    /* -------------------------------------------------- Entity Reference -------------------------------------------------- */
    
    // TODO: How do we do this with the converters?
    
//    @Pure
//    @Override
//    public final @Nonnull String getEntityReference() {
//        return "REFERENCES " + this + "role (role) ON DELETE CASCADE";
//    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Returns the name of this client that is used for accreditation.
     */
    @Pure
    public abstract @Nonnull @MaxSize(50) String getName();
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this client.
     */
    @Pure
    public abstract @Nonnull @DomainName @MaxSize(63) String getIdentifier();
    
    /* -------------------------------------------------- Schema Name -------------------------------------------------- */
    
    @Pure
    @Override
    @Derive("identifier.replace(\".\", \"_\")")
    public abstract @Nonnull @CodeIdentifier @MaxSize(63) @Unequal("general") String getSchemaName();
    
    /* -------------------------------------------------- Preferred Permissions -------------------------------------------------- */
    
    /**
     * Returns the preferred permissions of this client.
     */
    @Pure
    public abstract @Nonnull @Frozen ReadOnlyAgentPermissions getPreferredPermissions();
    
    /* -------------------------------------------------- Secret -------------------------------------------------- */
    
    protected final @Nonnull WritableVolatileValueProperty<@Nonnull Exponent> protectedSecret = WritableVolatileValuePropertyBuilder.withValue(ExponentBuilder.withValue(BigInteger.ZERO).build()).build();
    
    /**
     * Stores the secret of this client.
     */
    public final @Nonnull ReadOnlyVolatileValueProperty<@Nonnull Exponent> secret = protectedSecret;
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    protected void initialize() throws ExternalException {
        protectedSecret.set(ClientSecretLoader.load(getIdentifier()));
    }
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns a new commitment for the given subject with the given secret.
     */
    @Pure
    @NonCommitting
    protected @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject, @Nonnull Exponent secret) throws ExternalException {
        final @Nonnull HostIdentity host = subject.getHostIdentifier().resolve();
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(host, time);
        final @Nonnull Element value = publicKey.getAu().pow(secret);
        return CommitmentBuilder.withHost(host).withTime(time).withValue(value.getValue()).withPublicKey(publicKey).build();
    }
    
    /**
     * Returns a new commitment for the given subject.
     */
    @Pure
    @NonCommitting
    public @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject) throws ExternalException {
        return getCommitment(subject, protectedSecret.get());
    }
    
    /**
     * Rotates the secret of this client.
     * 
     * TODO: Make sure that other instances of the same client learn about the key rotation.
     */
    @Committing
    @PureWithSideEffects
    public void rotateSecret() throws InterruptedException, ExternalException {
        final @Nonnull Exponent newSecret = ExponentBuilder.withValue(new BigInteger(Parameters.HASH.get(), new SecureRandom())).build();
        final @Nonnull ReadOnlySet<NativeRole> roles = roles().get();
        Database.commit();
        
        for (@Nonnull NativeRole role : roles) {
//            final @Nonnull Commitment newCommitment = getCommitment(role.getIssuer().getAddress(), newSecret);
            // TODO:
//            role.getAgent().setCommitment(newCommitment);
        }
        
        for (@Nonnull NativeRole role : roles) {
            // TODO?
//            role.waitForCompletion(CoreService.SERVICE);
        }
        
        ClientSecretLoader.store(getIdentifier(), newSecret);
        protectedSecret.set(newSecret);
    }
    
    /* -------------------------------------------------- Roles -------------------------------------------------- */
    
    /**
     * Returns the native roles of this client.
     */
    @Pure
    @GeneratePersistentProperty
    public abstract @Nonnull WritablePersistentSimpleSetProperty<Client, NativeRole> roles();
    
    // TODO: Remove the following code once the writable extensible property for roles is implemented.
    
//    /**
//     * Returns the roles of this client.
//     */
//    @Pure
//    @NonCommitting
//    public @Nonnull @NonFrozen @UniqueElements ReadOnlyList<@Nonnull NativeRole> getRoles() throws DatabaseException {
//        if (Database.isMultiAccess()) { return RoleModule.getRoles(this); }
//        if (roles == null) { roles = RoleModule.getRoles(this); }
//        return roles;
//    }
//    
//    /**
//     * Adds the given role to the roles of this client.
//     * 
//     * @param issuer the issuer of the role to add.
//     * @param agentNumber the agent number of the role to add.
//     * 
//     * @return the newly created role of this client.
//     */
//    @Impure
//    @NonCommitting
//    private @Nonnull NativeRole addRole(@Nonnull InternalNonHostIdentity issuer, long agentNumber) throws DatabaseException {
//        final @Nonnull NativeRole role = NativeRole.add(this, issuer, agentNumber);
//        if (Database.isSingleAccess()) { role.observe(this, Role.DELETED); }
//        
//        if (roles != null) { roles.add(role); }
////        notify(ROLE_ADDED);
//        return role;
//    }
    
//    @Override
//    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
//        if (aspect.equals(Role.DELETED) && roles != null) { roles.remove(instance); }
//    }
    
}
