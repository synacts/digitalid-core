package net.digitalid.core.host;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.collection.ReadOnlyCollection;
import net.digitalid.utility.collections.map.FreezableLinkedHashMapBuilder;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.conversion.exceptions.ConversionException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.property.value.ReadOnlyVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValuePropertyBuilder;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Committing;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.client.Client;
import net.digitalid.core.entity.factories.HostFactory;
import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.request.RequestExceptionBuilder;
import net.digitalid.core.host.account.HostAccount;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.service.Service;
import net.digitalid.core.unit.CoreUnit;

/**
 * A host stores a {@link KeyPair} and is run by a server.
 * 
 * TODO: Make sure that the host keys get rotated!
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Host extends CoreUnit {
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this host.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getIdentifier();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Remove this method once derivation statements can throw exceptions", date = "2016-12-14", author = Author.KASPAR_ETTER)
    @Nonnull HostIdentity deriveIdentity(@Nonnull HostIdentifier identifier) {
        try {
            return identifier.resolve();
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Returns the identity of this host.
     */
    @Pure
    @Derive("deriveIdentity(identifier)") // TODO: The identity has to be mapped and not resolved (Mapper.mapHostIdentity(identifier)).
    public abstract @Nonnull HostIdentity getIdentity();
    
    /* -------------------------------------------------- Account -------------------------------------------------- */
    
    /**
     * Returns the account of this host.
     */
    @Pure
    @Derive("HostAccount.with(this, identity)")
    public abstract @Nonnull HostAccount getAccount();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    @Derive("identifier.asSchemaName()")
    public abstract @Nonnull @CodeIdentifier @MaxSize(61) @Unequal("general") String getName();
    
    /* -------------------------------------------------- Private Key -------------------------------------------------- */
    
    protected final @Nonnull WritableVolatileValueProperty<@Nonnull PrivateKeyChain> protectedPrivateKeyChain = WritableVolatileValuePropertyBuilder.withValue((PrivateKeyChain) null /* TODO: PrivateKeyChainLoader.load(getIdentifier()) */).build();
    
    /**
     * Stores the private key chain of this host.
     */
    public final @Nonnull ReadOnlyVolatileValueProperty<@Nonnull PrivateKeyChain> privateKeyChain = protectedPrivateKeyChain;
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */
    
    protected final @Nonnull WritableVolatileValueProperty<@Nonnull PublicKeyChain> protectedPublicKeyChain = WritableVolatileValuePropertyBuilder.withValue((PublicKeyChain) null /* TODO: PublicKeyChainLoader.load(getIdentifier()) */).build();
    
    /**
     * Stores the public key chain of this host.
     */
    public final @Nonnull ReadOnlyVolatileValueProperty<@Nonnull PublicKeyChain> publicKeyChain = protectedPublicKeyChain;
    
    /* -------------------------------------------------- Client -------------------------------------------------- */
    
    /**
     * Returns the client associated with this host.
     */
    @Pure
    // TODO: What is a good prefix for the identifier of the client?
    // TODO: What are the right permissions to pass here? Probably an aggregation of all the services.
    @Derive("net.digitalid.core.client.ClientBuilder.withIdentifier(\"client.\" + identifier.getString()).withDisplayName(\"Host \" + identifier.getString()).withPreferredPermissions(net.digitalid.core.permissions.ReadOnlyAgentPermissions.GENERAL_WRITE).build()")
    public abstract @Nonnull Client getClient();
    
    /* -------------------------------------------------- Hosts -------------------------------------------------- */
    
    /**
     * Maps the identifiers of the hosts that are running on this server to their instances.
     */
    private static final @Nonnull FreezableMap<@Nonnull HostIdentifier, @Nonnull Host> hosts = FreezableLinkedHashMapBuilder.build();
    
    /**
     * Returns whether the host with the given identifier is running on this server.
     */
    @Pure
    public static boolean exists(@Nonnull HostIdentifier identifier) {
        return hosts.containsKey(identifier);
    }
    
    /**
     * Returns all the hosts that are running on this server.
     */
    @Pure
    public static @Nonnull ReadOnlyCollection<@Nonnull Host> getAll() {
        return hosts.values();
    }
    
    /**
     * Returns the host with the given identifier.
     * 
     * @throws RequestException if there is no host with the given identifier on this server.
     */
    @Pure
    public static @Nonnull Host of(@Nonnull HostIdentifier identifier) throws RequestException {
        final @Nullable Host host = hosts.get(identifier);
        if (host == null) { throw RequestExceptionBuilder.withCode(RequestErrorCode.RECIPIENT).withMessage("The host '" + identifier.getString() + "' does not exist on this server.").build(); }
        return host;
    }
    
    /* -------------------------------------------------- Initializer -------------------------------------------------- */
    
    /**
     * Initializes the host factory.
     */
    @PureWithSideEffects
    @Initialize(target = HostFactory.class)
    public static void initializeHostFactory() {
        HostFactory.configuration.set(Host::of);
    }
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    protected void initialize() throws ConversionException {
        super.initialize();
        
        protectedPrivateKeyChain.set(PrivateKeyChainLoader.load(getIdentifier()));
        protectedPublicKeyChain.set(PublicKeyChainLoader.load(getIdentifier()));
        
        hosts.put(getIdentifier(), this);
        
        // TODO:
//        try {
//            final @Nonnull Attribute attribute = Attribute.of(getAccount(), PublicKeyChain.TYPE);
//            if (attribute.value().get() == null) {
//                final @Nonnull AttributeValue value;
//                if (Server.hasHost(HostIdentifier.DIGITALID) || identifier.equals(HostIdentifier.DIGITALID)) {
//                    // If the new host is running on the same server as 'core.digitalid.net', certify its public key immediately.
//                    value = new CertifiedAttributeValue(publicKeyChain, identity, PublicKeyChain.TYPE);
//                } else {
//                    value = new UncertifiedAttributeValue(publicKeyChain);
//                }
//                attribute.value().set(value);
//            }
//        } catch (@Nonnull DatabaseException exception) {
//            throw new RuntimeException(exception); // TODO
//        }
    }
    
    /* -------------------------------------------------- CoreUnit -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean isHost() {
        return true;
    }
    
    @Pure
    @Override
    public final boolean isClient() {
        return false;
    }
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Returns whether this host hosts the given identity (in the core service).
     */
    @Pure
    public boolean hosts(@Nonnull InternalIdentity identity) {
        return identity.getAddress().getHostIdentifier().equals(getIdentifier());
    }
    
    /**
     * Returns whether this host supports the given service.
     */
    @Pure
    public boolean supports(@Nonnull Service service) {
        return true; // TODO!
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    /**
     * Loads all hosts with cryptographic keys but without a tables file in the hosts directory.
     */
    @Impure
    @Committing
    @TODO(task = "Implement this as an initializer?", date = "2017-03-21", author = Author.KASPAR_ETTER)
    public static void loadHosts() {
//        // TODO: Remove this special case when the certification mechanism is implemented.
//        final @Nonnull File digitalid = new File(Directory.getHostsDirectory().getPath() + File.separator + HostIdentifier.DIGITALID.getString() + ".private.xdf");
//        if (digitalid.exists() && digitalid.isFile()) {
//            try {
//                if (!new File(Directory.getHostsDirectory().getPath() + File.separator + HostIdentifier.DIGITALID.getString() + ".tables.xdf").exists()) { new Host(HostIdentifier.DIGITALID); }
//            } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//                throw InitializationError.get("Could not load the host configured in the file '" + digitalid.getName() + "'.", exception);
//            }
//        }
//        
//        final @Nonnull File[] files = Directory.getHostsDirectory().listFiles();
//        for (final @Nonnull File file : files) {
//            final @Nonnull String name = file.getName();
//            if (file.isFile() && name.endsWith(".private.xdf") && !name.equals(HostIdentifier.DIGITALID.getString() + ".private.xdf")) { // TODO: Remove the special case eventually.
//                try {
//                    final @Nonnull HostIdentifier identifier = new HostIdentifier(name.substring(0, name.length() - 12));
//                    if (!new File(Directory.getHostsDirectory().getPath() + File.separator + identifier.getString() + ".tables.xdf").exists()) { new Host(identifier); }
//                } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//                    throw InitializationError.get("Could not load the host configured in the file '" + name + "'.", exception);
//                }
//            }
//        }
    }
    
}
