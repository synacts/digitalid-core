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
package net.digitalid.core.testing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.errors.SupportErrorBuilder;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.testing.DatabaseTest;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.TypeLoader;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.testing.providers.TestIdentifierResolverBuilder;
import net.digitalid.core.testing.providers.TestPrivateKeyRetrieverBuilder;
import net.digitalid.core.testing.providers.TestPublicKeyRetrieverBuilder;
import net.digitalid.core.testing.providers.TestTypeLoaderBuilder;

/**
 * The base class for all unit tests that need to resolve identifiers or retrieve public and private keys.
 */
@Stateless
public abstract class CoreTest extends DatabaseTest {
    
    /* -------------------------------------------------- Parameters -------------------------------------------------- */
    
    /**
     * Initializes the parameters.
     */
    @PureWithSideEffects
    @Initialize(target = Parameters.class)
    public static void initializeParameters() {
        Parameters.FACTOR.set(520);
    
        Parameters.EXPONENT.set(128);
        
        Parameters.RANDOM_EXPONENT.set(256);
        Parameters.CREDENTIAL_EXPONENT.set(256);
    
        Parameters.RANDOM_CREDENTIAL_EXPONENT.set(384);
        Parameters.BLINDING_EXPONENT.set(384);
        
        Parameters.RANDOM_BLINDING_EXPONENT.set(512);
        Parameters.VERIFIABLE_ENCRYPTION.set(512);
        Parameters.SYMMETRIC_KEY.set(128);
        
        Parameters.HASH_SIZE.set(128);
        Parameters.HASH_FUNCTION.set(() -> {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (@Nonnull NoSuchAlgorithmException exception) {
                throw SupportErrorBuilder.withMessage("The hashing algorithm 'MD5' is not supported on this platform.").withCause(exception).build();
            }
        });
    }
    
    /* -------------------------------------------------- Identification -------------------------------------------------- */
    
    /**
     * Initializes the identifier resolver.
     */
    @PureWithSideEffects
    @Initialize(target = IdentifierResolver.class)
    public static void initializeIdentifierResolver() {
        if (!IdentifierResolver.configuration.isSet()) { IdentifierResolver.configuration.set(TestIdentifierResolverBuilder.build()); }
    }
    
    /**
     * Initializes the type loader.
     */
    @PureWithSideEffects
    @Initialize(target = TypeLoader.class)
    public static void initializeTypeLoader() {
        if (!TypeLoader.configuration.isSet()) { TypeLoader.configuration.set(TestTypeLoaderBuilder.build()); }
    }
    
    /* -------------------------------------------------- Key Retrievers -------------------------------------------------- */
    
    public static final @Nonnull Configuration<KeyPair> keyPair = Configuration.withUnknownProvider();
    
    /**
     * Initializes the key pair.
     */
    @PureWithSideEffects
    @Initialize(target = CoreTest.class, dependencies = Parameters.class)
    public static void initializeKeyPair() {
        keyPair.set(KeyPair.withRandomValues());
    }
    
    /**
     * Initializes the public key retriever.
     */
    @PureWithSideEffects
    @Initialize(target = PublicKeyRetriever.class, dependencies = CoreTest.class)
    public static void initializePublicKeyRetriever() {
        if (!PublicKeyRetriever.configuration.isSet()) { PublicKeyRetriever.configuration.set(TestPublicKeyRetrieverBuilder.withKeyPair(keyPair.get()).build()); }
    }
    
    /**
     * Initializes the private key retriever.
     */
    @PureWithSideEffects
    @Initialize(target = PrivateKeyRetriever.class, dependencies = CoreTest.class)
    public static void initializePrivateKeyRetriever() {
        if (!PrivateKeyRetriever.configuration.isSet()) { PrivateKeyRetriever.configuration.set(TestPrivateKeyRetrieverBuilder.withKeyPair(keyPair.get()).build()); }
    }
    
}
