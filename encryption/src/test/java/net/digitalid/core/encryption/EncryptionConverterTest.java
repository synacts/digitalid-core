package net.digitalid.core.encryption;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.string.Strings;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorBuilder;
import net.digitalid.core.symmetrickey.InitializationVectorConverter;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;

import org.junit.Test;

public class EncryptionConverterTest extends CryptographyTestBase {
    
    @Pure
    public <@Unspecifiable TYPE> void assertEncryption(@Nonnull Converter<TYPE, Void> converter, @NonCaptured @Unmodified @Nonnull TYPE object) throws ExternalException {
        final @Nonnull HostIdentifier recipient = HostIdentifier.with("digitalid.net");
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull RequestEncryption<TYPE> encryption = RequestEncryptionBuilder.withObject(object).withRecipient(recipient).withTime(time).withSymmetricKey(symmetricKey).withInitializationVector(initializationVector).build();
        final @Nonnull RequestEncryptionConverter<TYPE> encryptionConverter = RequestEncryptionConverterBuilder.withObjectConverter(converter).build();
        final @Nonnull byte[] bytes = XDF.convert(encryptionConverter, encryption);
        final @Nonnull RequestEncryption<TYPE> recoveredEncryption = XDF.recover(encryptionConverter, null, bytes);
        
        assertEquals(recipient, recoveredEncryption.getRecipient());
        assertEquals(symmetricKey, recoveredEncryption.getSymmetricKey());
        assertEquals(initializationVector, recoveredEncryption.getInitializationVector());
        assertEquals(object, recoveredEncryption.getObject());
    }
    
    @Test
    public void testEncryptionConverterWithStrings() throws ExternalException {
        assertEncryption(StringConverter.INSTANCE, "Hello World!");
        assertEncryption(StringConverter.INSTANCE, "Let's look whether it also works with longer messages.");
        assertEncryption(StringConverter.INSTANCE, Strings.repeat("A short sentence. ", 100));
    }
    
    @Test
    public void testEncryptionConverterWithInitializationVector() throws ExternalException {
        assertEncryption(InitializationVectorConverter.INSTANCE, InitializationVectorBuilder.build());
    }
    
}
