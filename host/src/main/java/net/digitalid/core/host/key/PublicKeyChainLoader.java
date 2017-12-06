/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.host.key;

import java.io.File;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
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
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.keychain.PublicKeyChainConverter;
import net.digitalid.core.pack.Pack;

/**
 * The public key chain loader loads and stores the public key chain of a host.
 */
@Immutable
@GenerateSubclass
public abstract class PublicKeyChainLoader {
    
    /* -------------------------------------------------- File -------------------------------------------------- */
    
    /**
     * Returns the file in which the public key chain of the host with the given identifier is stored.
     */
    @PureWithSideEffects
    public static @Nonnull @Absolute @ExistentParent File getFile(@Nonnull HostIdentifier identifier) {
        return Files.relativeToConfigurationDirectory(identifier.getString() + ".public.xdf");
    }
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Returns the public key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public @Nonnull PublicKeyChain getPublicKeyChain(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        final @Nonnull File file = getFile(identifier);
        if (!file.exists()) { KeyPairGenerator.generateKeyPairFor(identifier); }
        return Pack.loadFrom(file).unpack(PublicKeyChainConverter.INSTANCE, null);
    }
    
    /**
     * Sets the public key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public void setPublicKeyChain(@Nonnull HostIdentifier identifier, @Nonnull PublicKeyChain publicKeyChain) throws FileException {
        publicKeyChain.pack().storeTo(getFile(identifier));
    }
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configured public key chain loader.
     */
    public static final @Nonnull Configuration<PublicKeyChainLoader> configuration = Configuration.<PublicKeyChainLoader>with(new PublicKeyChainLoaderSubclass()).addDependency(Files.directory);
    
    /* -------------------------------------------------- Type Mapping -------------------------------------------------- */
    
    /**
     * Maps the converter with which a public key chain is unpacked.
     */
    @PureWithSideEffects
    @Initialize(target = PublicKeyChainLoader.class, dependencies = IdentifierResolver.class)
    public static void mapConverter() {
        PublicKeyChain.TYPE.isLoaded();
    }
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Loads the public key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public static @Nonnull PublicKeyChain load(@Nonnull HostIdentifier identifier) throws FileException, RecoveryException {
        return configuration.get().getPublicKeyChain(identifier);
    }
    
    /**
     * Stores the public key chain of the host with the given identifier.
     */
    @PureWithSideEffects
    public static void store(@Nonnull HostIdentifier identifier, @Nonnull PublicKeyChain publicKeyChain) throws FileException {
        configuration.get().setPublicKeyChain(identifier, publicKeyChain);
    }
    
}
