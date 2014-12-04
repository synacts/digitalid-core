package ch.virtualid.server;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.client.Client;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.concepts.Certificate;
import ch.virtualid.cryptography.KeyPair;
import ch.virtualid.cryptography.PrivateKeyChain;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.io.Directory;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.Service;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * A host stores a {@link KeyPair} and is run by a {@link Server}.
 * 
 * TODO: Make sure that the host keys get rotated!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Host extends Site {
    
    @Pure
    @Override
    public @Nonnull String getReference() {
        return Mapper.REFERENCE;
    }
    
    
    /*
    TODO: Support:
    services
    provider
    tokens
    members
    open/close
    */
    
    /**
     * Stores the identifier of this host.
     */
    private final @Nonnull HostIdentifier identifier;
    
    /**
     * Stores the identity of this host.
     */
    private final @Nonnull HostIdentity identity;
    
    /**
     * Stores the private key chain of this host.
     */
    private @Nonnull PrivateKeyChain privateKeyChain;
    
    /**
     * Stores the public key chain of this host.
     */
    private @Nonnull PublicKeyChain publicKeyChain;
    
    /**
     * Stores the client associated with this host.
     */
    private final @Nonnull Client client;
    
    /**
     * Creates a new host with the given identifier by either reading the cryptographic keys from the file system or creating them.
     * 
     * @param identifier the identifier of the new host.
     */
    public Host(@Nonnull HostIdentifier identifier) throws SQLException, IOException, PacketException, ExternalException {
        super(identifier.asHostName());
        
        this.identifier = identifier;
        this.identity = Mapper.mapHostIdentity(identifier);
        
        final @Nonnull String path = Directory.HOSTS.getPath() + Directory.SEPARATOR + identifier.getString();
        final @Nonnull File privateKeyFile = new File(path + ".private.xdf");
        final @Nonnull File publicKeyFile = new File(path + ".public.xdf");
        
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            this.privateKeyChain = new PrivateKeyChain(new SelfcontainedWrapper(new FileInputStream(privateKeyFile), true).getElement().checkType(PrivateKeyChain.TYPE));
            this.publicKeyChain = new PublicKeyChain(new SelfcontainedWrapper(new FileInputStream(publicKeyFile), true).getElement().checkType(PublicKeyChain.TYPE));
        } else {
            final @Nonnull KeyPair keyPair = new KeyPair();
            final @Nonnull Time time = new Time();
            this.privateKeyChain = new PrivateKeyChain(time, keyPair.getPrivateKey());
            this.publicKeyChain = new PublicKeyChain(time, keyPair.getPublicKey());
        }
        
        final @Nonnull SelfcontainedWrapper privateKeyWrapper = new SelfcontainedWrapper(SelfcontainedWrapper.SELFCONTAINED, privateKeyChain);
        final @Nonnull SelfcontainedWrapper publicKeyWrapper = new SelfcontainedWrapper(Attribute.TYPE, publicKeyChain);
        
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            privateKeyWrapper.write(new FileOutputStream(privateKeyFile), true);
            publicKeyWrapper.write(new FileOutputStream(publicKeyFile), true);
        }
        
        final @Nonnull Account account = Account.get(this, identity);
        final @Nonnull Attribute attribute = Attribute.get(account, PublicKeyChain.TYPE);
        if (attribute.getValue() == null) {
            final @Nonnull SignatureWrapper certificate;
            if (Server.hasHost(HostIdentifier.VIRTUALID)) {
                // If the new host is running on the same server as virtualid.ch, certify its public key immediately.
                certificate = new HostSignatureWrapper(Certificate.TYPE, publicKeyWrapper, identifier, PublicKeyChain.IDENTIFIER);
            } else {
                certificate = new SignatureWrapper(Certificate.TYPE, publicKeyWrapper, null);
            }
            attribute.replaceValue(null, certificate);
        }
        
        // TODO: Load which services this host runs and initialize them afterwards.
        CoreService.SERVICE.createTables(this);
        
        // TODO: What are the right permissions to pass here?
        this.client = new Client("_" + identifier.asHostName(), identifier.getString(), new Image("/ch/virtualid/resources/Host.png"), AgentPermissions.GENERAL_WRITE);
        
        Server.addHost(this);
    }
    
    /**
     * Returns the identifier of this host.
     * 
     * @return the identifier of this host.
     */
    @Pure
    public @Nonnull HostIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the identity of this host.
     * 
     * @return the identity of this host.
     */
    @Pure
    public @Nonnull HostIdentity getIdentity() {
        return identity;
    }
    
    /**
     * Returns the account of this host.
     * 
     * @return the account of this host.
     */
    @Pure
    public @Nonnull Account getAccount() {
        return Account.get(this, identity);
    }
    
    /**
     * Returns the private key chain of this host.
     * 
     * @return the private key chain of this host.
     */
    @Pure
    public @Nonnull PrivateKeyChain getPrivateKeyChain() {
        return privateKeyChain;
    }
    
    /**
     * Returns the public key chain of this host.
     * 
     * @return the public key chain of this host.
     */
    @Pure
    public @Nonnull PublicKeyChain getPublicKeyChain() {
        return publicKeyChain;
    }
    
    /**
     * Returns the client associated with this host.
     * 
     * @return the client associated with this host.
     */
    @Pure
    public @Nonnull Client getClient() {
        return client;
    }
    
    
    /**
     * Returns whether this host hosts the given identity.
     * 
     * @param identity the identity to check.
     * 
     * @return whether this host hosts the given identity.
     */
    @Pure
    public boolean hosts(@Nonnull InternalIdentity identity) {
        return identity.getAddress().getHostIdentifier().equals(identifier);
    }
    
    
    @Pure
    public boolean supports(final @Nonnull Service service) {
        return true; // TODO!
    }
    
}
