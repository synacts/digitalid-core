package net.digitalid.core.signature.host;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;

import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.InternalIdentifier;

import org.junit.Assert;
import org.junit.Test;

public class HostSignatureConverterTest extends CryptographyTestBase {
    
    @Test
    public void shouldSignAndVerify() throws Exception {
        final @Nonnull String message = "This is an authentic message";
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        final @Nonnull InternalIdentifier signer = InternalIdentifier.with("alice@digitalid.net");
        
        final @Nonnull HostSignature<@Nonnull String> signedIdentifier = HostSignatureBuilder.withObject(message).withSubject(subject).withSigner(signer).withTime(TimeBuilder.build()).build();
        
        final @Nonnull byte[] bytes = XDF.convert(HostSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedIdentifier);
        Assert.assertTrue(bytes.length > 0);
        
        final @Nonnull HostSignature<String> recoveredObject = XDF.recover(HostSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
        assertEquals(message, recoveredObject.getObject());
    }
    
}
