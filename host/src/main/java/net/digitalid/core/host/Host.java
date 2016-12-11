package net.digitalid.core.host;


import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.property.value.ReadOnlyVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValuePropertyBuilder;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.subject.site.Site;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.client.Client;
import net.digitalid.core.host.account.HostAccount;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.service.Service;

/**
 * A host stores a {@link KeyPair} and is run by a {@link Server}.
 * 
 * TODO: Make sure that the host keys get rotated!
 */
@Immutable
// TODO: @GenerateSubclass
public abstract class Host extends RootClass implements Site {
    
//    @Pure
//    @Override
//    public @Nonnull String getEntityReference() {
//        return Mapper.REFERENCE;
//    }
    
    /*
    TODO: Support:
    services
    provider
    tokens
    members
    open/close
    */
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this host.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getIdentifier();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    /**
     * Returns the identity of this host.
     */
    @Pure
    // TODO: @Derive("Mapper.mapHostIdentity(identifier)")
    public abstract @Nonnull HostIdentity getIdentity();
    
    /* -------------------------------------------------- Account -------------------------------------------------- */
    
    /**
     * Returns the account of this host.
     */
    @Pure
    @Derive("HostAccount.with(this, identity)")
    public abstract @Nonnull HostAccount getAccount();
    
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
    // TODO: @Derive
    public abstract @Nonnull Client getClient();
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    // TODO: Move the loading of the key chains to a separate class.
    
//    /**
//     * Creates a new host with the given identifier by either reading the cryptographic keys from the file system or creating them.
//     * 
//     * @param identifier the identifier of the new host.
//     */
//    @Committing
//    public Host(@Nonnull HostIdentifier identifier) throws IOException, ExternalException {
//        super(identifier.asHostName());
//        
//        this.identifier = identifier;
//        this.identity = Mapper.mapHostIdentity(identifier);
//        
//        final @Nonnull String path = Directory.getHostsDirectory().getPath() + File.separator + identifier.getString();
//        final @Nonnull File privateKeyFile = new File(path + ".private.xdf");
//        final @Nonnull File publicKeyFile = new File(path + ".public.xdf");
//        
//        if (privateKeyFile.exists() && publicKeyFile.exists()) {
//            this.privateKeyChain = PrivateKeyChain.XDF_CONVERTER.decodeNonNullable(None.OBJECT, SelfcontainedWrapper.decodeBlockFrom(new FileInputStream(privateKeyFile), true).checkType(PrivateKeyChain.TYPE));
//            this.publicKeyChain = PublicKeyChain.XDF_CONVERTER.decodeNonNullable(None.OBJECT, SelfcontainedWrapper.decodeBlockFrom(new FileInputStream(publicKeyFile), true).checkType(PublicKeyChain.TYPE));
//        } else {
//            final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
//            final @Nonnull Time time = TimeBuilder.get().build();
//            this.privateKeyChain = PrivateKeyChain.get(time, keyPair.getPrivateKey());
//            this.publicKeyChain = PublicKeyChain.get(time, keyPair.getPublicKey());
//        }
//        
//        final @Nonnull Block privateKeyBlock = SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, privateKeyChain);
//        final @Nonnull Block publicKeyBlock = SelfcontainedWrapper.encodeNonNullable(SelfcontainedWrapper.DEFAULT, publicKeyChain);
//        
//        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
//            privateKeyBlock.writeTo(new FileOutputStream(privateKeyFile), true);
//            publicKeyBlock.writeTo(new FileOutputStream(publicKeyFile), true);
//        }
//        
//        CoreService.INSTANCE.createTables(this);
//        Server.addHost(this);
//        Database.commit();
//        
//        final @Nonnull HostAccount account = HostAccount.get(this, identity);
//        final @Nonnull Attribute attribute = Attribute.get(account, PublicKeyChain.TYPE);
//        if (attribute.getValue() == null) {
//            final @Nonnull AttributeValue value;
//            if (Server.hasHost(HostIdentifier.DIGITALID) || identifier.equals(HostIdentifier.DIGITALID)) {
//                // If the new host is running on the same server as 'core.digitalid.net', certify its public key immediately.
//                value = new CertifiedAttributeValue(publicKeyChain, identity, PublicKeyChain.TYPE);
//            } else {
//                value = new UncertifiedAttributeValue(publicKeyChain);
//            }
//            attribute.replaceValue(null, value);
//        }
//        
//        // TODO: Load which services this host runs and initialize them afterwards.
//        Database.commit();
//        
//        // TODO: What are the right permissions to pass here? Probably an aggregation of all the services.
//        this.client = Client("_" + identifier.asHostName(), identifier.getString(), FreezableAgentPermissions.GENERAL_WRITE);
//    }
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Returns whether this host hosts the given identity.
     */
    @Pure
    public boolean hosts(@Nonnull InternalIdentity identity) {
        return identity.getAddress().getHostIdentifier().equals(getIdentifier());
    }
    
    
    @Pure
    public boolean supports(final @Nonnull Service service) {
        return true; // TODO!
    }
    
}
