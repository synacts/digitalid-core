package net.digitalid.core.host.key;

import java.io.File;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.file.Files;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.file.existence.ExistentParent;
import net.digitalid.utility.validation.annotations.file.path.Absolute;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.conversion.exceptions.FileException;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PrivateKeyChainConverter;
import net.digitalid.core.pack.Pack;

/**
 * The private key chain loader loads and stores the private key chain of a host.
 */
@Immutable
@GenerateSubclass
public abstract class PrivateKeyChainLoader {
    
    /* -------------------------------------------------- File -------------------------------------------------- */
    
    /**
     * Returns the file in which the private key chain of the host with the given identifier is stored.
     */
    @PureWithSideEffects
    public static @Nonnull @Absolute @ExistentParent File getFile(@Nonnull HostIdentifier identifier) {
        return Files.relativeToConfigurationDirectory(identifier.getString() + ".private.xdf");
    }
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the private key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public @Nonnull PrivateKeyChain getPrivateKeyChain(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        final @Nonnull File file = getFile(identifier);
        if (!file.exists()) { KeyPairGenerator.generateKeyPairFor(identifier); }
        return Pack.loadFrom(file).unpack(PrivateKeyChainConverter.INSTANCE, null);
    }
    
    /**
     * Sets the private key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public void setPrivateKeyChain(@Nonnull HostIdentifier identifier, @Nonnull PrivateKeyChain privateKeyChain) throws FileException {
        privateKeyChain.pack().storeTo(getFile(identifier));
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
    @PureWithSideEffects
    public static @Nonnull PrivateKeyChain load(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        return configuration.get().getPrivateKeyChain(identifier);
    }
    
    /**
     * Stores the private key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public static void store(@Nonnull HostIdentifier identifier, @Nonnull PrivateKeyChain privateKeyChain) throws FileException {
        configuration.get().setPrivateKeyChain(identifier, privateKeyChain);
    }
    
}
