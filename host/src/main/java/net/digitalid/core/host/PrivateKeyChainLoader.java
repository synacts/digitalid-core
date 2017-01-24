package net.digitalid.core.host;

import java.io.File;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PrivateKeyChainConverter;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.pack.Pack;

/**
 * The private key chain loader loads and stores the private key chain of a host.
 */
@Mutable
public class PrivateKeyChainLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the private key chain of the host with the given identifier.
     */
    @Pure
    @TODO(task = "Throw a net.digitalid.core.conversion.FileException instead.", date = "2016-12-14", author = Author.KASPAR_ETTER)
    public @Nonnull PrivateKeyChain getPrivateKeyChain(@Nonnull HostIdentifier identifier) throws ExternalException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier.getString() + ".private.xdf");
        if (file.exists()) {
            // TODO: Check the type of the loaded pack?
            return Pack.loadFrom(file).unpack(PrivateKeyChainConverter.INSTANCE, null);
        } else {
            final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull PrivateKeyChain privateKeyChain = PrivateKeyChain.with(time, keyPair.getPrivateKey());
            final @Nonnull PublicKeyChain publicKeyChain = PublicKeyChain.with(time, keyPair.getPublicKey());
            PrivateKeyChainLoader.store(identifier, privateKeyChain);
            PublicKeyChainLoader.store(identifier, publicKeyChain);
            return privateKeyChain;
        }
    }
    
    /**
     * Sets the private key chain of the host with the given identifier.
     */
    @Impure
    @TODO(task = "Throw a net.digitalid.core.conversion.FileException instead.", date = "2016-12-14", author = Author.KASPAR_ETTER)
    public void setPrivateKeyChain(@Nonnull HostIdentifier identifier, @Nonnull PrivateKeyChain privateKeyChain) throws ExternalException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier.getString() + ".private.xdf");
        Pack.pack(privateKeyChain, PrivateKeyChainConverter.INSTANCE).storeTo(file);
    }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configured private key chain loader.
     */
    public static final @Nonnull Configuration<PrivateKeyChainLoader> configuration = Configuration.with(new PrivateKeyChainLoader());
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the private key chain of the host with the given identifier.
     */
    @Pure
    public static @Nonnull PrivateKeyChain load(@Nonnull HostIdentifier identifier) throws ExternalException {
        return configuration.get().getPrivateKeyChain(identifier);
    }
    
    /**
     * Stores the private key chain of the host with the given identifier.
     */
    @Impure
    public static void store(@Nonnull HostIdentifier identifier, @Nonnull PrivateKeyChain privateKeyChain) throws ExternalException {
        configuration.get().setPrivateKeyChain(identifier, privateKeyChain);
    }
    
}
