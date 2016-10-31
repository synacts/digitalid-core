package net.digitalid.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class EncryptionConverterTest extends CryptographyTestBase {
    
    @Test
//    @Pure
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
        XDF.convert(encryptedIdentifier, EncryptionConverter.getInstance(IdentifierConverter.INSTANCE), byteArrayOutputStream);
    
        final @Nonnull byte[] encryptedBytes = byteArrayOutputStream.toByteArray();
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedBytes);
        final @Nullable Encryption<Identifier> encryption = XDF.recover(EncryptionConverter.getInstance(IdentifierConverter.INSTANCE), byteArrayInputStream);
    
        Assert.assertNotNull(encryption);
        Assert.assertEquals(identifierRecipient, encryption.getRecipient());
        Assert.assertEquals(symmetricKey, encryption.getSymmetricKey());
        Assert.assertEquals(initializationVector, encryption.getInitializationVector());
        Assert.assertEquals(identifier, encryption.getObject());
    }
    
}
