package net.digitalid.core.encryption;

import javax.annotation.Nonnull;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.IdentifierConverter;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorBuilder;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;

import org.junit.Test;

/**
 *
 */
public class EncryptionConverterTest extends CryptographyTestBase {
    
    @Test
    public void shouldConvertEncryptedObject() throws Exception {
        // The identifier is the secret that we are encrypting.
        // TODO: this is probably confusing and we should change it to a secret-message object.
        final @Nonnull Identifier identifier = Identifier.with("alice@digitalid.net");
        final @Nonnull HostIdentifier identifierRecipient = HostIdentifier.with("digitalid.net");
        
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull Encryption<Identifier> encryptedIdentifier = EncryptionBuilder.withObject(identifier).withTime(time).withRecipient(identifierRecipient).withSymmetricKey(symmetricKey).withInitializationVector(initializationVector).build();
    
        final @Nonnull byte[] encryptedBytes = XDF.convert(EncryptionConverter.getInstance(IdentifierConverter.INSTANCE), encryptedIdentifier);
    
        final @Nonnull Encryption<Identifier> encryption = XDF.recover(EncryptionConverter.getInstance(IdentifierConverter.INSTANCE), null, encryptedBytes);
    
        assertEquals(identifierRecipient, encryption.getRecipient());
        assertEquals(symmetricKey, encryption.getSymmetricKey());
        assertEquals(initializationVector, encryption.getInitializationVector());
        assertEquals(identifier, encryption.getObject());
    }
    
}
