package net.digitalid.core.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.cryptography.InitializationVector;
import net.digitalid.utility.cryptography.InitializationVectorBuilder;
import net.digitalid.utility.cryptography.Parameters;
import net.digitalid.utility.cryptography.SymmetricKey;
import net.digitalid.utility.cryptography.SymmetricKeyBuilder;
import net.digitalid.utility.cryptography.key.KeyPair;
import net.digitalid.utility.cryptography.key.PublicKey;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.conversion.converter.XDF;
import net.digitalid.core.cryptography.encryption.Encryption;
import net.digitalid.core.cryptography.encryption.EncryptionBuilder;
import net.digitalid.core.cryptography.encryption.EncryptionConverter;
import net.digitalid.core.identification.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.IdentifierConverter;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.HostIdentityBuilder;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class EncryptionConverterTest {
    
    /* -------------------------------------------------- Public Key Retriever -------------------------------------------------- */
    
    public static class PublicKeyRetrieverForTest implements PublicKeyRetriever {
        
        public final @Nonnull KeyPair keyPair;
        
        PublicKeyRetrieverForTest() {
            configuration.set(this);
            this.keyPair = KeyPair.withRandomValues();
        }
        
        @Pure
        @Override
        public @Nonnull PublicKey getPublicKey(@Nonnull HostIdentity host, @Nonnull Time time) throws ExternalException {
            return keyPair.getPublicKey();
        }
    }
    
    private static @Nonnull PublicKeyRetrieverForTest publicKeyRetrieverForTest;
    
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
        publicKeyRetrieverForTest = new PublicKeyRetrieverForTest();
    }
    
    @Test
    public void shouldConvertEncryptedObject() throws Exception {
    
        // The identifier is the secret that we're sending.
        // TODO: this is probably confusing and we should change it to a secret-message object.
        final @Nonnull Identifier identifier = Identifier.with("alice@digitalid.net");
        final @Nonnull HostIdentifier identifierRecipient = HostIdentifier.with("digitalid.net");
        
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull Encryption<Identifier> encryptedIdentifier = EncryptionBuilder.<Identifier>withTime(time).withRecipient(identifierRecipient).withSymmetricKey(symmetricKey).withInitializationVector(initializationVector).withObject(identifier).build();
        
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(encryptedIdentifier, EncryptionConverter.getInstance(IdentifierConverter.INSTANCE, publicKeyRetrieverForTest.keyPair.getPrivateKey()), byteArrayOutputStream);
    
        final @Nonnull byte[] encryptedBytes = byteArrayOutputStream.toByteArray();
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedBytes);
        final @Nullable Encryption<Identifier> encryption = XDF.recover(EncryptionConverter.getInstance(IdentifierConverter.INSTANCE, publicKeyRetrieverForTest.keyPair.getPrivateKey()), byteArrayInputStream);
    
        Assert.assertNotNull(encryption);
        Assert.assertEquals(identifierRecipient, encryption.getRecipient());
        Assert.assertEquals(symmetricKey, encryption.getSymmetricKey());
        Assert.assertEquals(initializationVector, encryption.getInitializationVector());
        Assert.assertEquals(identifier, encryption.getObject());
    }
    
}
