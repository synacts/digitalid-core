package ch.virtualid.server;

import ch.virtualid.cryptography.KeyPair;
import ch.virtualid.cryptography.PrivateKey;
import ch.virtualid.cryptography.PrivateKeyChain;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.cryptography.PublicKeyChain;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.io.Directory;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.FailedEncodingException;
import ch.xdf.exceptions.InvalidEncodingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * A host is run by a {@link Server} and handles the database queries that are related to the hosting of virtual identities.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.6
 */
public final class Host extends Site {
    
    @Override
    public @Nonnull String getReference() {
        return "REFERENCES map_identity (identity)";
    }
    
    
    /**
     * Stores the identifier of this host.
     */
    private final @Nonnull HostIdentifier identifier;
    
    /**
     * Stores the identity of this host.
     */
    private final @Nonnull HostIdentity self;
    
    /**
     * Stores the private key of this host.
     */
    private final @Nonnull PrivateKey privateKey;
    
    /**
     * Stores the public key of this host.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Creates a new host with the given identifier by either reading the cryptographic keys from the file system or creating them.
     * 
     * @param identifier the identifier of the new host.
     */
    public Host(@Nonnull HostIdentifier identifier) throws SQLException, IOException, InvalidEncodingException, FailedEncodingException {
        this.identifier = identifier;
        this.self = Mapper.mapHostIdentity(identifier);
        
        @Nonnull String path = Directory.HOSTS.getPath() + Directory.SEPARATOR + identifier.getString();
        @Nonnull File privateFile = new File(path + ".private.xdf");
        @Nonnull File publicFile = new File(path + ".public.xdf");
        
        if (privateFile.exists() && publicFile.exists()) {
            privateKey = new PrivateKey(SelfcontainedWrapper.readAndClose(new FileInputStream(privateFile)).getElement());
            publicKey = new PublicKey(SelfcontainedWrapper.readAndClose(new FileInputStream(publicFile)).getElement());
        } else {
            @Nonnull KeyPair keyPair = new KeyPair();
            privateKey = keyPair.getPrivateKey();
            publicKey = keyPair.getPublicKey();
        }
        
        SelfcontainedWrapper privateKeyWrapper = new SelfcontainedWrapper(NonHostIdentifier.HOST_PRIVATE_KEY, privateKey);
        SelfcontainedWrapper publicKeyWrapper = new SelfcontainedWrapper(NonHostIdentifier.HOST_PUBLIC_KEY, publicKey);
        
        if (!privateFile.exists() || !publicFile.exists()) {
            privateKeyWrapper.writeAndClose(new FileOutputStream(privateFile));
            publicKeyWrapper.writeAndClose(new FileOutputStream(publicFile));
        }
        
        try (@Nonnull Connection connection = Database.getConnection()) {
            if (getAttributeValue(connection, self, SemanticType.HOST_PUBLIC_KEY, true) == null) {
                Blockable attribute;
                if (Server.hasHost(HostIdentifier.VIRTUALID)) {
                    // If the new host is running on the same server as virtualid.ch, certify its public key immediately.
                    attribute = new HostSignatureWrapper(publicKeyWrapper, identifier, HostIdentifier.VIRTUALID);
                } else {
                    attribute = new SignatureWrapper(publicKeyWrapper, null);
                }
                setAttributeValue(connection, self, SemanticType.HOST_PUBLIC_KEY, true, attribute.toBlock());
            }
            connection.commit();
        }
        
        Server.addHost(this);
    }
    
    /**
     * Returns the identifier of this host.
     * 
     * @return the identifier of this host.
     */
    public @Nonnull HostIdentifier getIdentifier() {
        return identifier;
    }
    
    /**
     * Returns the identity of this host.
     * 
     * @return the identity of this host.
     */
    public @Nonnull HostIdentity getIdentity() {
        return self;
    }
    
    /**
     * Returns the private key of this host.
     * 
     * @return the private key of this host.
     */
    public @Nonnull PrivateKeyChain getPrivateKeyChain() {
        return privateKey;
    }
    
    /**
     * Returns the public key of this host.
     * 
     * @return the public key of this host.
     */
    public @Nonnull PublicKeyChain getPublicKeyChain() {
        return publicKey;
    }
    
    /**
     * Returns whether this host hosts the given identity.
     * 
     * @param identity the identity to check.
     * @return whether this host hosts the given identity.
     */
    public boolean hosted(@Nonnull Identity identity) {
        return identity.getAddress().getHostIdentifier().equals(identifier);
    }
    
}
