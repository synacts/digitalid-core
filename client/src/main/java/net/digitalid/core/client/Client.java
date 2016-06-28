package net.digitalid.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.cryptography.key.PublicKey;
import net.digitalid.utility.directory.Directory;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.math.Element;
import net.digitalid.utility.math.Exponent;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.Site;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.client.commitment.Commitment;
import net.digitalid.core.client.commitment.CommitmentBuilder;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.xdf.Encode;
import net.digitalid.core.entity.NativeRole;
import net.digitalid.core.entity.Role;
import net.digitalid.core.entity.RoleModule;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;

/**
 * A client is configured with an identifier and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 */
@Mutable
@GenerateSubclass
public abstract class Client implements Site {
    
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
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    // TODO: Remove the following code.
    
//    /**
//     * Stores the semantic type {@code secret.client@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType SECRET = SemanticType.map("secret.client@core.digitalid.net").load(Exponent.TYPE);
//    
//    /**
//     * Stores the semantic type {@code name.client.agent@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType NAME = SemanticType.map("name.client.agent@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
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
    
    @Pure
    @Override
    public final @Nonnull String getEntityReference() {
        return "REFERENCES " + this + "role (role) ON DELETE CASCADE";
    }
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this client.
     */
    @Pure
    public abstract @Nonnull String getIdentifier();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    /**
     * Returns the name of this client.
     */
    @Pure
    public abstract @Nonnull @MaxSize(50) String getName();
    
    /**
     * Sets the name of this client.
     */
    @Impure
    public abstract void setName(@Nonnull @MaxSize(50) String name);
    
    /* -------------------------------------------------- Preferred Permissions -------------------------------------------------- */
    
    /**
     * Returns the preferred permissions of this client.
     */
    @Pure
    public abstract @Nonnull @Frozen ReadOnlyAgentPermissions getPreferredPermissions();
    
    /* -------------------------------------------------- Secret -------------------------------------------------- */
    
    @Pure
    static @Nonnull Exponent loadSecret(@Nonnull @DomainName String identifier) {
        final @Nonnull File file = new File(Directory.getClientsDirectory().getPath() + File.separator + identifier + ".client.xdf");
        if (file.exists()) {
            this.secret = Exponent.get(SelfcontainedWrapper.decodeBlockFrom(new FileInputStream(file), true).checkType(SECRET));
        } else {
            this.secret = Exponent.get(new BigInteger(Parameters.HASH, new SecureRandom()));
            SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, Encode.nonNullable(SECRET, secret)).writeTo(new FileOutputStream(file), true);
        }
    }
    
    /**
     * Returns the secret of this client.
     */
    @Pure
    public abstract @Nonnull Exponent getSecret();
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns a new commitment for the given subject with the given secret.
     */
    @Pure
    @NonCommitting
    private static @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject, @Nonnull Exponent secret) throws ExternalException {
        final @Nonnull HostIdentity host = subject.getHostIdentifier().getIdentity();
        final @Nonnull Time time = TimeBuilder.get().build();
        final @Nonnull PublicKey publicKey = Cache.getPublicKeyChain(host).getKey(time);
        final @Nonnull Element value = publicKey.getAu().pow(secret);
        return CommitmentBuilder.withHost(host).withTime(time).withValue(value.getValue()).build();
    }
    
    /**
     * Returns a new commitment for the given subject.
     */
    @Pure
    @NonCommitting
    public final @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject) throws ExternalException {
        return getCommitment(subject, secret);
    }
    
    /**
     * Rotates the secret of this client.
     * 
     * TODO: Make sure that other instances of the same client learn about the key rotation.
     */
    @Committing
    public final void rotateSecret() throws InterruptedException, ExternalException {
        final @Nonnull Exponent newSecret = Exponent.get(new BigInteger(Parameters.HASH, new SecureRandom()));
        final @Nonnull ReadOnlyList<NativeRole> roles = getRoles();
        Database.commit();
        
        for (final @Nonnull NativeRole role : roles) {
            final @Nonnull Commitment newCommitment = getCommitment(role.getIssuer().getAddress(), newSecret);
            role.getAgent().setCommitment(newCommitment);
        }
        
        for (final @Nonnull NativeRole role : roles) {
            role.waitForCompletion(CoreService.SERVICE);
        }
        
        this.secret = newSecret;
        final @Nonnull File file = new File(Directory.getClientsDirectory().getPath() + File.separator + identifier + ".client.xdf");
        SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, secret.toBlock().setType(SECRET)).writeTo(new FileOutputStream(file), true);
    }
    
    /* -------------------------------------------------- Roles -------------------------------------------------- */
    
    /**
     * Stores the roles of this client.
     * 
     * @invariant roles == null || roles.doesNotContainNull() : "The roles do not contain null.";
     * @invariant roles == null || roles.doesNotContainDuplicates() : "The roles do not contain duplicates.";
     */
    private @Nullable @NonFrozen FreezableList<NativeRole> roles;
    
    /**
     * Returns the roles of this client.
     * 
     * @return the roles of this client.
     * 
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     * @ensure return.doesNotContainDuplicates() : "The returned list does not contain duplicates.";
     */
    @Pure
    @NonCommitting
    public final @Nonnull @NonFrozen ReadOnlyList<NativeRole> getRoles() throws DatabaseException {
        if (Database.isMultiAccess()) { return RoleModule.getRoles(this); }
        if (roles == null) { roles = RoleModule.getRoles(this); }
        return roles;
    }
    
    /**
     * Adds the given role to the roles of this client.
     * 
     * @param issuer the issuer of the role to add.
     * @param agentNumber the agent number of the role to add.
     * 
     * @return the newly created role of this client.
     */
    @NonCommitting
    private @Nonnull NativeRole addRole(@Nonnull InternalNonHostIdentity issuer, long agentNumber) throws DatabaseException {
        final @Nonnull NativeRole role = NativeRole.add(this, issuer, agentNumber);
        if (Database.isSingleAccess()) { role.observe(this, Role.DELETED); }
        
        if (roles != null) { roles.add(role); }
        notify(ROLE_ADDED);
        return role;
    }
    
    @Override
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
        if (aspect.equals(Role.DELETED) && roles != null) { roles.remove(instance); }
    }
    
    
}
