package ch.virtualid.client;

import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.database.Database;
import ch.virtualid.database.SQLiteConfiguration;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.action.internal.AccountClose;
import ch.virtualid.handler.action.internal.AccountInitialize;
import ch.virtualid.handler.action.internal.AccountOpen;
import ch.virtualid.handler.action.internal.ClientAgentAccredit;
import ch.virtualid.handler.query.internal.StateQuery;
import ch.virtualid.handler.reply.query.StateReply;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.Predecessor;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Directory;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.client.Roles;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.StringWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * A client is configured with an identifier and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class Client extends Site implements Observer {
    
    /**
     * Stores the aspect of a new role being added to the observed client.
     */
    public static final @Nonnull Aspect ROLE_ADDED = new Aspect(Client.class, "role added");
    
    
    /**
     * Stores the semantic type {@code secret.client@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SECRET = SemanticType.create("secret.client@virtualid.ch").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code name.client.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType NAME = SemanticType.create("name.client.agent@virtualid.ch").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code icon.client.agent@virtualid.ch}.
     */
    public static final @Nonnull SemanticType ICON = SemanticType.create("icon.client.agent@virtualid.ch").load(Image.TYPE);
    
    
    /**
     * The pattern that valid client identifiers have to match.
     */
    private static final @Nonnull Pattern PATTERN = Pattern.compile("[a-z_][a-z0-9_$]+", Pattern.CASE_INSENSITIVE);
    
    /**
     * Returns whether the given identifier is valid for clients.
     * 
     * @param identifier the client identifier to check for validity.
     * 
     * @return whether the given identifier is valid for clients.
     */
    public static boolean isValidIdentifier(@Nonnull String identifier) {
        return identifier.length() <= 40 && PATTERN.matcher(identifier).matches() && !identifier.equals("general") && !(Database.getConfiguration() instanceof SQLiteConfiguration && identifier.contains("$"));
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
    public static boolean isValid(@Nonnull String name) {
        return name.length() <= 50;
    }
    
    /**
     * Returns whether the given icon is valid.
     * A valid icon is a square of 256 pixels.
     * 
     * @param icon the icon to be checked.
     * 
     * @return whether the given name is valid.
     */
    @Pure
    public static boolean isValid(@Nonnull Image icon) {
        return icon.isSquare(256);
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
    private final @Nonnull Exponent secret;
    
    /**
     * Stores the name of this client.
     * 
     * @invariant isValid(name) : "The name is valid.";
     */
    private final @Nonnull String name;
    
    /**
     * Stores the icon of this client.
     * 
     * @invariant isValid(icon) : "The icon is valid.";
     */
    private final @Nonnull Image icon;
    
    /**
     * Stores the preferred permissions of this client.
     * 
     * @invariant preferredPermissions.isFrozen() : "The preferred permissions are frozen.";
     */
    private final @Nonnull ReadonlyAgentPermissions preferredPermissions;
    
    /**
     * Creates a new client with the given identifier.
     * 
     * @param identifier the identifier of the new client.
     * @param name the name of the new client.
     * @param icon the icon of the new client.
     * @param preferredPermissions the preferred permissions of the new client.
     * 
     * @require isValidIdentifier(identifier) : "The identifier is valid.";
     * @require isValid(name) : "The name is valid.";
     * @require isValid(icon) : "The icon is valid.";
     * @require preferredPermissions.isFrozen() : "The preferred permissions are frozen.";
     */
    public Client(@Nonnull String identifier, @Nonnull String name, @Nonnull Image icon, @Nonnull ReadonlyAgentPermissions preferredPermissions) throws SQLException, IOException, PacketException, ExternalException {
        super(identifier);
        
        assert isValidIdentifier(identifier) : "The identifier is valid.";
        assert isValid(name) : "The name is valid.";
        assert isValid(icon) : "The icon is valid.";
        assert preferredPermissions.isFrozen() : "The preferred permissions are frozen.";
        
        this.identifier = identifier;
        this.name = name;
        this.icon = icon;
        this.preferredPermissions = preferredPermissions;
        
        final @Nonnull File file = new File(Directory.CLIENTS.getPath() +  Directory.SEPARATOR + identifier + ".client.xdf");
        if (file.exists()) {
            this.secret = new Exponent(new SelfcontainedWrapper(new FileInputStream(file), true).getElement().checkType(SECRET));
        } else {
            final @Nonnull Random random = new SecureRandom();
            this.secret = new Exponent(new BigInteger(Parameters.HASH, random));
            new SelfcontainedWrapper(SelfcontainedWrapper.SELFCONTAINED, secret.toBlock().setType(SECRET)).write(new FileOutputStream(file), true);
        }
        
        // The role table needs to be created in advance.
        Roles.createTable(this);
        CoreService.SERVICE.createTables(this);
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
    public @Nonnull String getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the secret of this client.
     * 
     * @return the secret of this client.
     */
    @Pure
    public @Nonnull Exponent getSecret() {
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
     * Returns the icon of this client.
     * 
     * @return the icon of this client.
     * 
     * @ensure isValid(return) : "The returned icon is valid.";
     */
    @Pure
    public final @Nonnull Image getIcon() {
        return icon;
    }
    
    /**
     * Returns the preferred permissions of this client.
     * 
     * @return the preferred permissions of this client.
     * 
     * @ensure return.isFrozen() : "The preferred permissions are frozen.";
     */
    @Pure
    public final @Nonnull ReadonlyAgentPermissions getPreferredPermissions() {
        return preferredPermissions;
    }
    
    
    /**
     * Returns a new commitment for the given subject.
     * 
     * @param subject the subject for which to commit.
     * 
     * @return a new commitment for the given subject.
     */
    @Pure
    public final @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull HostIdentity host = subject.getHostIdentifier().getIdentity();
        final @Nonnull Time time = new Time();
        final @Nonnull PublicKey publicKey = Cache.getPublicKeyChain(host).getKey(time);
        final @Nonnull Element value = publicKey.getAu().pow(secret);
        return new Commitment(host, time, value, publicKey);
    }
    
    
    /**
     * Stores the roles of this client.
     * 
     * @invariant roles == null || roles.isNotFrozen() : "The roles are not frozen.";
     * @invariant roles == null || roles.doesNotContainNull() : "The roles do not contain null.";
     * @invariant roles == null || roles.doesNotContainDuplicates() : "The roles do not contain duplicates.";
     * @invariant roles == null || for (Role role : roles) role.isNative() : "Every role in the roles is native.";
     */
    private @Nullable FreezableList<Role> roles;
    
    /**
     * Returns the roles of this client.
     * 
     * @return the roles of this client.
     * 
     * @ensure return.isNotFrozen() : "The returned list is not frozen.";
     * @ensure return.doesNotContainNull() : "The returned list does not contain null.";
     * @ensure return.doesNotContainDuplicates() : "The returned list does not contain duplicates.";
     * @ensure for (Role role : return) role.isNative() : "Every role in the returned list is native.";
     */
    @Pure
    public final @Nonnull ReadonlyList<Role> getRoles() throws SQLException {
        if (roles == null) roles = Roles.getRoles(this);
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
    private @Nonnull Role addRole(@Nonnull InternalNonHostIdentity issuer, long agentNumber) throws SQLException {
        final @Nonnull Role role = Role.add(this, issuer, null, null, agentNumber);
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
     * Loop on {@link Role#isAccredited()} afterwards.
     * 
     * @param identity the identity at which this client is to be accredited.
     * @param password the password for accreditation at the given identity.
     * 
     * @return the role which was accredited at the given identity.
     * 
     * @require Password.isValid(password) : "The password is valid.";
     */
    public final @Nonnull Role accredit(@Nonnull InternalNonHostIdentity identity, @Nonnull String password) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull Role role = addRole(identity, new SecureRandom().nextLong());
        Database.commit();
        try {
            final @Nonnull ClientAgentAccredit action = new ClientAgentAccredit(role, password);
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
     * @return the role of this client at the newly created account.
     * 
     * @require subject.doesNotExist() : "The subject does not exist.";
     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     * @require !category.isType() || roles.size() <= 1 && identifiers.isEmpty() : "If the category denotes a type, at most one role and no identifier may be given.";
     */
    public final @Nonnull Role openAccount(@Nonnull InternalNonHostIdentifier subject, @Nonnull Category category, @Nonnull ReadonlyList<Role> roles, @Nonnull ReadonlyList<ExternalIdentifier> identifiers) throws SQLException, IOException, PacketException, ExternalException {
        assert subject.doesNotExist() : "The subject does not exist.";
        assert category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
        assert !category.isType() || roles.size() <= 1 && identifiers.isEmpty() : "If the category denotes a type, at most one role and no identifier may be given.";
        
        Database.commit();
        final @Nonnull AccountOpen accountOpen = new AccountOpen(subject, category, this); accountOpen.send();
        final @Nonnull InternalNonHostIdentity identity = (InternalNonHostIdentity) Mapper.mapIdentity(subject, category, null);
        final @Nonnull Role newRole = addRole(identity, accountOpen.getAgentNumber());
        accountOpen.initialize(newRole);
        Database.commit();
        
        final @Nonnull FreezableList<Pair<Predecessor, Block>> states = new FreezableArrayList<Pair<Predecessor, Block>>(roles.size() + identifiers.size());
        
        for (final @Nonnull Role role : roles) {
            if (role.isNotNative()) throw new PacketException(PacketError.INTERNAL, "Only native roles can be merged.");
            if (role.getIdentity().getCategory() != category) throw new PacketException(PacketError.INTERNAL, "A role is of the wrong category.");
            final @Nonnull StateReply reply = new StateQuery(role).sendNotNull(); // TODO: Store the reply permanently?
            final @Nonnull Predecessor predecessor = new Predecessor(role.getIdentity().getAddress());
            states.add(new Pair<Predecessor, Block>(predecessor, reply.toBlock()));
            Database.commit();
            Synchronizer.execute(new AccountClose(role, subject));
            role.remove();
        }
        
        for (final @Nonnull ExternalIdentifier identifier : identifiers) {
            // TODO: Ask 'virtualid.ch' to let the relocation be confirmed by the user.
        }
        
        for (final @Nonnull ExternalIdentifier identifier : identifiers) {
            // TODO: Wait until the relocation from 'virtualid.ch' can be verified.
            states.add(new Pair<Predecessor, Block>(new Predecessor(identifier), null));
        }
        
        Synchronizer.execute(new AccountInitialize(newRole, states.freeze()));
        return newRole;
    }
    
    /**
     * Opens a new account with the given identifier and category.
     * 
     * @param identifier the identifier of the account to be created.
     * @param category the category of the account to be created.
     * 
     * @return the role of this client at the newly created account.
     * 
     * @require subject.doesNotExist() : "The subject does not exist.";
     * @require category.isInternalNonHostIdentity() : "The category denotes an internal non-host identity.";
     */
    public final @Nonnull Role openAccount(@Nonnull InternalNonHostIdentifier identifier, @Nonnull Category category) throws SQLException, IOException, PacketException, ExternalException {
        return openAccount(identifier, category, new FreezableLinkedList<Role>().freeze(), new FreezableLinkedList<ExternalIdentifier>().freeze());
    }
    
}
