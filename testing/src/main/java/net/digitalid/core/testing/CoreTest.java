package net.digitalid.core.testing;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.configuration.Configuration;
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
        Parameters.FACTOR.set(130);
        Parameters.RANDOM_EXPONENT.set(64);
        Parameters.CREDENTIAL_EXPONENT.set(64);
        Parameters.RANDOM_CREDENTIAL_EXPONENT.set(96);
        Parameters.BLINDING_EXPONENT.set(96);
        Parameters.RANDOM_BLINDING_EXPONENT.set(128);
        Parameters.VERIFIABLE_ENCRYPTION.set(128);
        Parameters.SYMMETRIC_KEY.set(128);
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
