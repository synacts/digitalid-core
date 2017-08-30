package net.digitalid.core.host;

import java.io.File;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PrivateKeyChainConverter;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.pack.Pack;

/**
 * The private key chain loader loads and stores the private key chain of a host.
 */
@Immutable
@GenerateSubclass
public abstract class PrivateKeyChainLoader {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the private key chain of the host with the given identifier.
     */
    @Pure
    public @Nonnull PrivateKeyChain getPrivateKeyChain(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier.getString() + ".private.xdf");
        if (file.exists()) {
            // TODO: Check the type of the loaded pack?
            return Pack.loadFrom(file).unpack(PrivateKeyChainConverter.INSTANCE, null);
        } else {
            final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
            final @Nonnull Time time = TimeBuilder.build().subtract(Time.SECOND).roundDown(Time.DAY);
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
    @PureWithSideEffects
    public void setPrivateKeyChain(@Nonnull HostIdentifier identifier, @Nonnull PrivateKeyChain privateKeyChain) throws FileException {
        final @Nonnull File file = Files.relativeToConfigurationDirectory(identifier.getString() + ".private.xdf");
        Pack.pack(PrivateKeyChainConverter.INSTANCE, privateKeyChain).storeTo(file);
    }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configured private key chain loader.
     */
    public static final @Nonnull Configuration<PrivateKeyChainLoader> configuration = Configuration.<PrivateKeyChainLoader>with(new PrivateKeyChainLoaderSubclass()).addDependency(Files.directory);
    
    /* -------------------------------------------------- Type Mapping -------------------------------------------------- */
    
    /**
     * Maps the converter with which a private key chain is unpacked.
     */
    @PureWithSideEffects
    @Initialize(target = PrivateKeyChainLoader.class, dependencies = IdentifierResolver.class)
    @TODO(task = "Provide the correct parameters for the loading of the type.", date = "2017-08-30", author = Author.KASPAR_ETTER)
    public static void mapConverter() {
        SemanticType.map(PrivateKeyChainConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build());
    }
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the private key chain of the host with the given identifier.
     */
    @Pure
    public static @Nonnull PrivateKeyChain load(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        return configuration.get().getPrivateKeyChain(identifier);
    }
    
    /**
     * Stores the private key chain of the host with the given identifier.
     */
    @Impure
    public static void store(@Nonnull HostIdentifier identifier, @Nonnull PrivateKeyChain privateKeyChain) throws FileException {
        configuration.get().setPrivateKeyChain(identifier, privateKeyChain);
    }
    
}
