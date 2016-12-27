package net.digitalid.core.signature.host;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(signedIdentifier, HostSignatureConverter.getInstance(StringConverter.INSTANCE), byteArrayOutputStream);
    
        final @Nonnull byte[] signedBytes = byteArrayOutputStream.toByteArray();
        Assert.assertTrue(signedBytes.length > 0);
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(signedBytes);
        final @Nullable HostSignature<String> recoveredObject = XDF.recover(HostSignatureConverter.getInstance(StringConverter.INSTANCE), null, byteArrayInputStream);
        
        assertNotNull(recoveredObject);
        assertEquals(message, recoveredObject.getObject());
    }
}
