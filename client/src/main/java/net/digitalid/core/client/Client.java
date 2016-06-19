package net.digitalid.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.tuples.FreezablePair;
import net.digitalid.utility.collections.tuples.ReadOnlyPair;
import net.digitalid.utility.directory.Directory;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.NonFrozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.agent.ClientAgentAccredit;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.context.Context;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.conversion.xdf.Encode;
import net.digitalid.core.entity.NativeRole;
import net.digitalid.core.entity.Role;
import net.digitalid.core.entity.RoleModule;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identifier.ExternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.resolution.Category;
import net.digitalid.core.resolution.Mapper;
import net.digitalid.core.resolution.Predecessor;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.synchronizer.ResponseAudit;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.synchronizer.SynchronizerModule;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.cryptography.Element;
import net.digitalid.service.core.cryptography.Exponent;
import net.digitalid.service.core.cryptography.Parameters;
import net.digitalid.service.core.cryptography.PublicKey;

/**
 * A client is configured with an identifier and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 */
public class Client extends Site {
    
    /**
     * Stops the background threads of the client without shutting down.
     */
    public static void stop() {
        Database.stopPurging();
        Synchronizer.shutDown();
        ResponseAudit.shutDown();
    }
    
    
    /**
     * Stores the aspect of a new role being added to the observed client.
     */
    public static final @Nonnull Aspect ROLE_ADDED = new Aspect(Client.class, "role added");
    
    
    /**
     * Stores the semantic type {@code secret.client@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType SECRET = SemanticType.map("secret.client@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code name.client.agent@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NAME = SemanticType.map("name.client.agent@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    
    /**
     * Returns whether the given identifier is valid for clients.
     * 
     * @param identifier the client identifier to check for validity.
     * 
     * @return whether the given identifier is valid for clients.
     */
    public static boolean isValidIdentifier(@Nonnull String identifier) {
        return identifier.length() <= 40 && !identifier.equals("general") && Database.getConfiguration().isValidIdentifier(identifier);
    }
    
    /**
     * Returns whether the given name is valid.
     * A valid name has at most 50 characters.
     * 
     * @param name the name to be checked.
     * 
     * @return whether the given name is valid.
     */
    @Pure
    public static boolean isValidName(@Nonnull String name) {
        return name.length() <= 50;
    }
    
    
    @Pure
    @Override
    public final @Nonnull String getEntityReference() {
        return "REFERENCES " + this + "role (role) ON DELETE CASCADE";
    }
    
    
    /**
     * Stores the identifier of this client.
     * 
     * @invariant isValidIdentifier(identifier) : "The identifier is valid.";
     */
    private final @Nonnull String identifier;
    
    /**
     * Stores the secret of this client.
     */
    private @Nonnull Exponent secret;
    
    /**
     * Stores the name of this client.
     * 
     * @invariant isValid(name) : "The name is valid.";
     */
    private @Nonnull String name;
    
    /**
     * Stores the preferred permissions of this client.
     * 
     * @invariant preferredPermissions.isFrozen() : "The preferred permissions are frozen.";
     */
    private final @Nonnull ReadOnlyAgentPermissions preferredPermissions;
    
    /**
     * Creates a new client with the given identifier.
     * 
     * @param identifier the identifier of the new client.
     * @param name the name of the new client.
     * @param preferredPermissions the preferred permissions of the new client.
     */
    @Locked
    @Committing
    public Client(@Nonnull @Validated String identifier, @Nonnull @Validated String name, @Nonnull @Frozen ReadOnlyAgentPermissions preferredPermissions) throws IOException, ExternalException {
        super(identifier);
        
        Require.that(isValidIdentifier(identifier)).orThrow("The identifier is valid.");
        Require.that(isValidName(name)).orThrow("The name is valid.");
        Require.that(preferredPermissions.isFrozen()).orThrow("The preferred permissions are frozen.");
        
        this.identifier = identifier;
        this.name = name;
        this.preferredPermissions = preferredPermissions;
        
        final @Nonnull File file = new File(Directory.getClientsDirectory().getPath() + File.separator + identifier + ".client.xdf");
        if (file.exists()) {
            this.secret = Exponent.get(SelfcontainedWrapper.decodeBlockFrom(new FileInputStream(file), true).checkType(SECRET));
        } else {
            this.secret = Exponent.get(new BigInteger(Parameters.HASH, new SecureRandom()));
            SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, Encode.nonNullable(SECRET, secret)).writeTo(new FileOutputStream(file), true);
        }
        
        // The role table needs to be created in advance.
        RoleModule.createTable(this);
        CoreService.SERVICE.createTables(this);
        SynchronizerModule.load(this);
        Database.commit();
    }
    
    /**
     * Returns the identifier of this client.
     * 
     * @return the identifier of this client.
     * 
     * @ensure isValidIdentifier(identifier) : "The identifier is valid.";
     */
    @Pure
    public final @Nonnull String getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the secret of this client.
     * 
     * @return the secret of this client.
     */
    @Pure
    public final @Nonnull Exponent getSecret() {
        return secret;
    }
    
    /**
     * Returns the name of this client.
     * 
     * @return the name of this client.
     * 
     * @ensure isValid(return) : "The returned name is valid.";
     */
    @Pure
    public final @Nonnull String getName() {
        return name;
    }
    
    /**
     * Sets the name of this client.
     * 
     * @param name the name of this client.
     * 
     * @require isValid(name) : "The name is valid.";
     */
    public final void setName(@Nonnull String name) {
        Require.that(isValidName(name)).orThrow("The name is valid.");
        
        this.name = name;
    }
    
    /**
     * Returns the preferred permissions of this client.
     * 
     * @return the preferred permissions of this client.
     * 
     * @ensure return.isFrozen() : "The preferred permissions are frozen.";
     */
    @Pure
    public final @Nonnull ReadOnlyAgentPermissions getPreferredPermissions() {
        return preferredPermissions;
    }
    
    
    /**
     * Returns a new commitment for the given subject with the given secret.
     * 
     * @param subject the subject for which to commit the given secret.
     * @param secret the secret which is to be committed for the subject.
     * 
     * @return a new commitment for the given subject with the given secret.
     */
    @Pure
    @NonCommitting
    private static @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject, @Nonnull Exponent secret) throws ExternalException {
        final @Nonnull HostIdentity host = subject.getHostIdentifier().getIdentity();
        final @Nonnull Time time = Time.getCurrent();
        final @Nonnull PublicKey publicKey = Cache.getPublicKeyChain(host).getKey(time);
        final @Nonnull Element value = publicKey.getAu().pow(secret);
        return new Commitment(host, time, value, publicKey);
    }
    
    /**
     * Returns a new commitment for the given subject.
     * 
     * @param subject the subject for which to commit.
     * 
     * @return a new commitment for the given subject.
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
    
    
    /**
     * Accredits this client at the given identity.
     * Loop on {@link Role#reloadOrRefreshState(net.digitalid.service.core.service.Service...)} afterwards.
     * 
     * @param identity the identity at which this client is to be accredited.
     * @param password the password for accreditation at the given identity.
     * 
     * @return the native role which was accredited at the given identity.
     * 
     * @require Password.isValid(password) : "The password is valid.";
     */
    @Committing
    public final @Nonnull NativeRole accredit(@Nonnull InternalNonHostIdentity identity, @Nonnull String password) throws ExternalException {
        final @Nonnull NativeRole role = addRole(identity, new Random().nextLong());
        Database.commit();
        try {
            final @Nonnull ClientAgentAccredit action = new ClientAgentAccredit(role, password);
            Context.getRoot(role).createForActions();
            action.executeOnClient();
            action.send();
            Database.commit();
        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
            Database.rollback();
            role.remove();
            Database.commit();
            throw exception;
        }
        return role;
    }
    
    
    /**
     * Opens a new account with the given identifier and merges existing roles and identifiers into it.
     * 
     * TODO: Make the method more resilient to failures so that it can also be restarted mid-way through.
     * 
     * @param subject the identifier of the account which is to be created.
     * @param category the category of the account which is to be created.
     * @param roles the roles to be closed and merged into the new account.
     * @param identifiers the identifiers to be merged into the new account.
     * 
     * @return the native role of this client at the newly created account.
     * 
     * @require !subject.exists() : "The subject does not exist.";
     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     * @require !category.isType() || roles.size() <= 1 && identifiers.isEmpty() : "If the category denotes a type, at most one role and no identifier may be given.";
     */
    @Committing
    public final @Nonnull NativeRole openAccount(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull ReadOnlyList<NativeRole> roles, @Nonnull ReadOnlyList<ExternalIdentifier> identifiers) throws InterruptedException, ExternalException {
        Require.that(!subject.exists()).orThrow("The subject does not exist.");
        Require.that(category.isInternalNonHostIdentity()).orThrow("The category denotes an internal non-host identity.");
        Require.that(!category.isType() || roles.size() <= 1 && identifiers.isEmpty()).orThrow("If the category denotes a type, at most one role and no identifier may be given.");
        
        Database.commit(); // This commit is necessary in case the client and host share the same database.
        final @Nonnull AccountOpen accountOpen = new AccountOpen(subject, category, this); accountOpen.send();
        final @Nonnull InternalNonHostIdentity identity = (InternalNonHostIdentity) Mapper.mapIdentity(subject, category, null);
        final @Nonnull NativeRole newRole = addRole(identity, accountOpen.getAgentNumber());
        accountOpen.initialize(newRole);
        Database.commit();
        
        final @Nonnull FreezableList<ReadOnlyPair<Predecessor, Block>> states = FreezableArrayList.getWithCapacity(roles.size() + identifiers.size());
        
        for (final @Nonnull NativeRole role : roles) {
            if (role.getIdentity().getCategory() != category) { throw InternalException.get("A role is of the wrong category."); }
            Synchronizer.reload(role, CoreService.SERVICE);
            final @Nonnull ClientAgent clientAgent = role.getAgent();
            final @Nonnull Block state = CoreService.SERVICE.getState(role, clientAgent.getPermissions(), clientAgent.getRestrictions(), clientAgent);
            final @Nonnull Predecessor predecessor = new Predecessor(role.getIdentity().getAddress());
            states.add(new FreezablePair<>(predecessor, state).freeze());
            Synchronizer.execute(new AccountClose(role, subject));
            role.remove();
            Database.commit();
        }
        
        for (final @Nonnull ExternalIdentifier identifier : identifiers) {
            // TODO: Ask 'digitalid.net' to let the relocation be confirmed by the user.
        }
        
        for (final @Nonnull ExternalIdentifier identifier : identifiers) {
            // TODO: Wait until the relocation from 'digitalid.net' can be verified.
            states.add(new FreezablePair<Predecessor, Block>(new Predecessor(identifier), null).freeze());
        }
        
        final @Nonnull AccountInitialize accountInitialize = new AccountInitialize(newRole, states.freeze());
        accountInitialize.send();
        accountInitialize.executeOnClient();
        Database.commit();
        return newRole;
    }
    
    /**
     * Opens a new account with the given identifier and category.
     * 
     * @param identifier the identifier of the account to be created.
     * @param category the category of the account to be created.
     * 
     * @return the native role of this client at the newly created account.
     * 
     * @require subject.doesNotExist() : "The subject does not exist.";
     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    @Committing
    public final @Nonnull NativeRole openAccount(@Nonnull InternalNonHostIdentifier identifier, @Nonnull Category category) throws InterruptedException, ExternalException {
        return openAccount(identifier, category, new FreezableLinkedList<NativeRole>().freeze(), new FreezableLinkedList<ExternalIdentifier>().freeze());
    }
    
}
