package net.digitalid.core.testing;

import java.io.FileNotFoundException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
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

import org.junit.BeforeClass;

/**
 * The base class for all unit tests that need to resolve identifiers or retrieve public and private keys.
 */
@Stateless
public abstract class CoreTest extends DatabaseTest {
    
    private static boolean initialized = false;
    
    /**
     * Initializes the cryptographic parameters and all configurations of the core library.
     */
    @BeforeClass
    @PureWithSideEffects
    public static void initializeConfigurations() throws IllegalArgumentException, FileNotFoundException {
        if (!initialized) {
            Parameters.FACTOR.set(130);
            Parameters.RANDOM_EXPONENT.set(64);
            Parameters.CREDENTIAL_EXPONENT.set(64);
            Parameters.RANDOM_CREDENTIAL_EXPONENT.set(96);
            Parameters.BLINDING_EXPONENT.set(96);
            Parameters.RANDOM_BLINDING_EXPONENT.set(128);
            Parameters.VERIFIABLE_ENCRYPTION.set(128);
            Parameters.SYMMETRIC_KEY.set(128);
            
            if (!IdentifierResolver.configuration.isSet()) { IdentifierResolver.configuration.set(TestIdentifierResolverBuilder.build()); }
            if (!TypeLoader.configuration.isSet()) { TypeLoader.configuration.set(TestTypeLoaderBuilder.build()); }
            
            final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
            if (!PublicKeyRetriever.configuration.isSet()) { PublicKeyRetriever.configuration.set(TestPublicKeyRetrieverBuilder.withKeyPair(keyPair).build()); }
            if (!PrivateKeyRetriever.configuration.isSet()) { PrivateKeyRetriever.configuration.set(TestPrivateKeyRetrieverBuilder.withKeyPair(keyPair).build()); }
            
            initialized = true;
        }
    }
    
}
