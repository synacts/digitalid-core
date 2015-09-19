package net.digitalid.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.agent.ClientAgentAccredit;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.concept.Aspect;
import net.digitalid.core.concept.Instance;
import net.digitalid.core.concept.Observer;
import net.digitalid.core.contact.Context;
import net.digitalid.core.cryptography.Element;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.cryptography.Parameters;
import net.digitalid.core.cryptography.PublicKey;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NativeRole;
import net.digitalid.core.entity.Role;
import net.digitalid.core.entity.RoleModule;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.ExternalIdentifier;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.Predecessor;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.io.Directory;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.synchronizer.ResponseAudit;
import net.digitalid.core.synchronizer.Synchronizer;
import net.digitalid.core.synchronizer.SynchronizerModule;
import net.digitalid.core.tuples.FreezablePair;
import net.digitalid.core.tuples.ReadOnlyPair;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * A client is configured with an identifier and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class Client extends Site implements Observer {
    
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
    public static final @Nonnull SemanticType NAME = SemanticType.map("name.client.agent@core.digitalid.net").load(StringWrapper.TYPE);
    
    
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
    public Client(@Nonnull @Validated String identifier, @Nonnull @Validated String name, @Nonnull @Frozen ReadOnlyAgentPermissions preferredPermissions) throws SQLException, IOException, PacketException, ExternalException {
        super(identifier);
        
        assert isValidIdentifier(identifier) : "The identifier is valid.";
        assert isValidName(name) : "The name is valid.";
        assert preferredPermissions.isFrozen() : "The preferred permissions are frozen.";
        
        this.identifier = identifier;
        this.name = name;
        this.preferredPermissions = preferredPermissions;
        
        final @Nonnull File file = new File(Directory.getClientsDirectory().getPath() +  File.separator + identifier + ".client.xdf");
        if (file.exists()) {
            this.secret = new Exponent(new SelfcontainedWrapper(new FileInputStream(file), true).getElement().checkType(SECRET));
        } else {
            this.secret = new Exponent(new BigInteger(Parameters.HASH, new SecureRandom()));
            new SelfcontainedWrapper(SelfcontainedWrapper.DEFAULT, secret.toBlock().setType(SECRET)).write(new FileOutputStream(file), true);
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
        assert isValidName(name) : "The name is valid.";
        
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
    private static @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject, @Nonnull Exponent secret) throws SQLException, IOException, PacketException, ExternalException {
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
    public final @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject) throws SQLException, IOException, PacketException, ExternalException {
        return getCommitment(subject, secret);
    }
    
    /**
     * Rotates the secret of this client.
     * 
     * TODO: Make sure that other instances of the same client learn about the key rotation.
     */
    @Committing
    public final void rotateSecret() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        final @Nonnull Exponent newSecret = new Exponent(new BigInteger(Parameters.HASH, new SecureRandom()));
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
        final @Nonnull File file = new File(Directory.getClientsDirectory().getPath() +  File.separator + identifier + ".client.xdf");
        new SelfcontainedWrapper(SelfcontainedWrapper.DEFAULT, secret.toBlock().setType(SECRET)).write(new FileOutputStream(file), true);
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
    public final @Nonnull @NonFrozen ReadOnlyList<NativeRole> getRoles() throws SQLException {
        if (Database.isMultiAccess()) return RoleModule.getRoles(this);
        if (roles == null) roles = RoleModule.getRoles(this);
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
    private @Nonnull NativeRole addRole(@Nonnull InternalNonHostIdentity issuer, long agentNumber) throws SQLException {
        final @Nonnull NativeRole role = NativeRole.add(this, issuer, agentNumber);
        if (Database.isSingleAccess()) role.observe(this, Role.DELETED);
        
        if (roles != null) roles.add(role);
        notify(ROLE_ADDED);
        return role;
    }
    
    @Override
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
        if (aspect.equals(Role.DELETED) && roles != null) roles.remove(instance);
    }
    
    
    /**
     * Accredits this client at the given identity.
     * Loop on {@link Role#reloadOrRefreshState(net.digitalid.core.service.Service...)} afterwards.
     * 
     * @param identity the identity at which this client is to be accredited.
     * @param password the password for accreditation at the given identity.
     * 
     * @return the native role which was accredited at the given identity.
     * 
     * @require Password.isValid(password) : "The password is valid.";
     */
    @Committing
    public final @Nonnull NativeRole accredit(@Nonnull InternalNonHostIdentity identity, @Nonnull String password) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull NativeRole role = addRole(identity, new Random().nextLong());
        Database.commit();
        try {
            final @Nonnull ClientAgentAccredit action = new ClientAgentAccredit(role, password);
            Context.getRoot(role).createForActions();
            action.executeOnClient();
            action.send();
            Database.commit();
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
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
    public final @Nonnull NativeRole openAccount(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull ReadOnlyList<NativeRole> roles, @Nonnull ReadOnlyList<ExternalIdentifier> identifiers) throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        assert !subject.exists() : "The subject does not exist.";
        assert category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
        assert !category.isType() || roles.size() <= 1 && identifiers.isEmpty() : "If the category denotes a type, at most one role and no identifier may be given.";
        
        Database.commit(); // This commit is necessary in case the client and host share the same database.
        final @Nonnull AccountOpen accountOpen = new AccountOpen(subject, category, this); accountOpen.send();
        final @Nonnull InternalNonHostIdentity identity = (InternalNonHostIdentity) Mapper.mapIdentity(subject, category, null);
        final @Nonnull NativeRole newRole = addRole(identity, accountOpen.getAgentNumber());
        accountOpen.initialize(newRole);
        Database.commit();
        
        final @Nonnull FreezableList<ReadOnlyPair<Predecessor, Block>> states = new FreezableArrayList<>(roles.size() + identifiers.size());
        
        for (final @Nonnull NativeRole role : roles) {
            if (role.getIdentity().getCategory() != category) throw new PacketException(PacketError.INTERNAL, "A role is of the wrong category.");
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
    public final @Nonnull NativeRole openAccount(@Nonnull InternalNonHostIdentifier identifier, @Nonnull Category category) throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        return openAccount(identifier, category, new FreezableLinkedList<NativeRole>().freeze(), new FreezableLinkedList<ExternalIdentifier>().freeze());
    }
    
}
