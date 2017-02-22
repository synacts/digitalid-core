package net.digitalid.core.signature.host;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class HostSignatureConverterTest extends CoreTest {
    
    @Test
    public void shouldSignAndVerify() throws RecoveryException {
        final @Nonnull String message = "This is an authentic message.";
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        final @Nonnull InternalIdentifier signer = InternalIdentifier.with("alice@digitalid.net");
        
        final @Nonnull HostSignature<@Nonnull String> signedIdentifier = HostSignatureBuilder.withObject(message).withSubject(subject).withSigner(signer).build();
        
        final @Nonnull byte[] bytes = XDF.convert(HostSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedIdentifier);
        assertThat(bytes.length).isPositive();
        
        final @Nonnull HostSignature<String> recoveredObject = XDF.recover(HostSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
        assertThat(recoveredObject.getObject()).isEqualTo(message);
    }
    
}
