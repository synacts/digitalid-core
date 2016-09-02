package net.digitalid.core.conversion.transformational;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.cryptography.Parameters;
import net.digitalid.utility.cryptography.key.KeyPair;
import net.digitalid.utility.cryptography.key.PrivateKey;
import net.digitalid.utility.cryptography.key.PublicKey;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;

import net.digitalid.core.identification.PrivateKeyRetriever;
import net.digitalid.core.identification.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.NonHostIdentifier;
import net.digitalid.core.identification.identity.HostIdentityBuilder;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.NaturalPersonBuilder;

import org.junit.BeforeClass;

/**
 *
 */
public class CryptographyTestBase {
    
    /* -------------------------------------------------- Public Key Retriever -------------------------------------------------- */
    
    public static class PublicKeyRetrieverForTest implements PublicKeyRetriever {
        
        public final @Nonnull KeyPair keyPair;
        
        PublicKeyRetrieverForTest(@Nonnull KeyPair keyPair) {
            configuration.set(this);
            this.keyPair = keyPair;
        }
        
        @Pure
        @Override
        public @Nonnull PublicKey getPublicKey(@Nonnull InternalIdentity host, @Nonnull Time time) throws ExternalException {
            return keyPair.getPublicKey();
        }
        
    }
    
    protected static @Nonnull PublicKeyRetrieverForTest publicKeyRetrieverForTest;
    
    /* -------------------------------------------------- Private Key Retriever -------------------------------------------------- */
    
    public static class PrivateKeyRetrieverForTest implements PrivateKeyRetriever {
        
        public final @Nonnull KeyPair keyPair;
        
        PrivateKeyRetrieverForTest(@Nonnull KeyPair keyPair) {
            configuration.set(this);
            this.keyPair = keyPair;
        }
        
        @Pure
        @Override
        public @Nonnull PrivateKey getPrivateKey(@Nonnull Time time) throws ExternalException {
            return keyPair.getPrivateKey();
        }
        
    }
    
    protected static @Nonnull PrivateKeyRetrieverForTest privateKeyRetrieverForTest;
    
    /* -------------------------------------------------- Identifier Resolver -------------------------------------------------- */
    
    public static class IdentifierResolverForTest extends IdentifierResolver {
    
        IdentifierResolverForTest() {
            configuration.set(this);
        }
        
        @Pure
        @Override
        public @Nonnull Identity getIdentity(@Nonnull Identifier identifier) throws ExternalException {
            if (identifier instanceof HostIdentifier) {
                return HostIdentityBuilder.withKey(1L).withAddress((HostIdentifier) identifier).build();
            } else if (identifier instanceof InternalNonHostIdentifier) {
                // TODO: How do we know whether the identifier is one of a natural or artificial person or a type?
                return NaturalPersonBuilder.withKey(1L).withAddress((InternalNonHostIdentifier) identifier).build();
            } else {
                throw new UnsupportedOperationException("The identifier resolver does not support '" + identifier.getClass() + "' yet.");
            }
        }
        
    }
    
    private static @Nonnull IdentifierResolverForTest identifierResolverForTest;
    
    /* -------------------------------------------------- Setup -------------------------------------------------- */
    
    @Pure
    @BeforeClass
    public static void setup() throws Exception {
        Parameters.FACTOR.set(128);
        Parameters.RANDOM_EXPONENT.set(64);
        Parameters.CREDENTIAL_EXPONENT.set(64);
        Parameters.RANDOM_CREDENTIAL_EXPONENT.set(96);
        Parameters.BLINDING_EXPONENT.set(96);
        Parameters.RANDOM_BLINDING_EXPONENT.set(128);
        Parameters.VERIFIABLE_ENCRYPTION.set(128);
        Parameters.ENCRYPTION_KEY.set(128);
        
        identifierResolverForTest = new IdentifierResolverForTest();
        final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
        publicKeyRetrieverForTest = new PublicKeyRetrieverForTest(keyPair);
        privateKeyRetrieverForTest = new PrivateKeyRetrieverForTest(keyPair);
    }
    
}
