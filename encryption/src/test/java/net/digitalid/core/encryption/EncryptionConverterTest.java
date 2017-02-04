package net.digitalid.core.encryption;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.exceptions.ExternalException;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorBuilder;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;

import org.junit.Test;

public class EncryptionConverterTest extends CryptographyTestBase {
    
    @Test
    public void testEncryptionConverter() throws ExternalException {
        final @Nonnull String secret = "Hello World!";
        final @Nonnull HostIdentifier recipient = HostIdentifier.with("digitalid.net");
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull Encryption<String> encryption = EncryptionBuilder.withObject(secret).withRecipient(recipient).withTime(time).withSymmetricKey(symmetricKey).withInitializationVector(initializationVector).build();
        final @Nonnull EncryptionConverter<String> encryptionConverter = EncryptionConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build();
        final @Nonnull byte[] bytes = XDF.convert(encryptionConverter, encryption);
        final @Nonnull Encryption<String> recoveredEncryption = XDF.recover(encryptionConverter, null, bytes);
        
        assertEquals(recipient, recoveredEncryption.getRecipient());
        assertEquals(symmetricKey, recoveredEncryption.getSymmetricKey());
        assertEquals(initializationVector, recoveredEncryption.getInitializationVector());
        assertEquals(secret, recoveredEncryption.getObject());
    }
    
}
