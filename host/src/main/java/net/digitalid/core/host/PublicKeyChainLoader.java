package net.digitalid.core.host;

import java.io.File;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.keychain.PublicKeyChainConverter;
import net.digitalid.core.pack.Pack;

/**
 * The public key chain loader loads and stores the public key chain of a host.
 */
@Immutable
@GenerateSubclass
public abstract class PublicKeyChainLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the public key chain of the host with the given identifier.
     */
    @Pure
    public @Nonnull PublicKeyChain getPublicKeyChain(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier.getString() + ".public.xdf");
        if (file.exists()) {
            // TODO: Check the type of the loaded pack?
            return Pack.loadFrom(file).unpack(PublicKeyChainConverter.INSTANCE, null);
        } else {
            final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
            final @Nonnull Time time = TimeBuilder.build();
            final @Nonnull PrivateKeyChain privateKeyChain = PrivateKeyChain.with(time, keyPair.getPrivateKey());
            final @Nonnull PublicKeyChain publicKeyChain = PublicKeyChain.with(time, keyPair.getPublicKey());
            PrivateKeyChainLoader.store(identifier, privateKeyChain);
            PublicKeyChainLoader.store(identifier, publicKeyChain);
            return publicKeyChain;
        }
    }
    
    /**
     * Sets the public key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public void setPublicKeyChain(@Nonnull HostIdentifier identifier, @Nonnull PublicKeyChain publicKeyChain) throws FileException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier.getString() + ".public.xdf");
        Pack.pack(PublicKeyChainConverter.INSTANCE, publicKeyChain).storeTo(file);
    }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configured public key chain loader.
     */
    public static final @Nonnull Configuration<PublicKeyChainLoader> configuration = Configuration.<PublicKeyChainLoader>with(new PublicKeyChainLoaderSubclass()).addDependency(Files.directory);
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the public key chain of the host with the given identifier.
     */
    @Pure
    public static @Nonnull PublicKeyChain load(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        return configuration.get().getPublicKeyChain(identifier);
    }
    
    /**
     * Stores the public key chain of the host with the given identifier.
     */
    @Impure
    public static void store(@Nonnull HostIdentifier identifier, @Nonnull PublicKeyChain publicKeyChain) throws FileException {
        configuration.get().setPublicKeyChain(identifier, publicKeyChain);
    }
    
}
