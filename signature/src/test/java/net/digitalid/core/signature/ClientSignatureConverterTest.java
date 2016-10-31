package net.digitalid.core.signature;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.math.ExponentBuilder;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.commitment.SecretCommitmentBuilder;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.conversion.value.testentities.CustomString;
import net.digitalid.core.conversion.value.testentities.CustomStringBuilder;
import net.digitalid.core.conversion.value.testentities.CustomStringConverter;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.signature.ClientSignature;
import net.digitalid.core.signature.ClientSignatureConverter;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ClientSignatureConverterTest extends CryptographyTestBase {
    
    @Test
    public void shouldSignAndVerify() throws Exception {
        final @Nonnull CustomString message = CustomStringBuilder.withValue("This is a secret message").build();
        final @Nonnull HostIdentifier hostIdentifier = HostIdentifier.with("digitalid.net");
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        
        final @Nonnull Time time = TimeBuilder.buildWithValue(1472810684033L);
        final @Nonnull HostIdentity hostIdentity = (HostIdentity) IdentifierResolver.resolve(hostIdentifier);
        final @Nonnull PublicKey publicKey = publicKeyRetrieverForTest.getPublicKey(hostIdentity, time);
        
        // TODO: what to chose as value and secret?
//        final @Nonnull BigInteger value = new BigInteger("4");
        final @Nonnull Exponent secret = ExponentBuilder.withValue(BigInteger.TEN).build();
        
        final @Nonnull SecretCommitment commitment = SecretCommitmentBuilder.withHost(hostIdentity).withTime(time).withPublicKey(publicKey).withSecret(secret).build();
        
        final @Nonnull ClientSignature<CustomString> signedMessage = ClientSignatureBuilder.withElement(message).withSecretCommitment(commitment).withSubject(subject).withTime(time).build(); 
    
        final @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(signedMessage, ClientSignatureConverter.getInstance(CustomStringConverter.INSTANCE), byteArrayOutputStream);
    
        final @Nonnull byte[] signedBytes = byteArrayOutputStream.toByteArray();
        Assert.assertTrue(signedBytes.length > 0);
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(signedBytes);
        final ClientSignature<CustomString> recoveredObject = XDF.recover(ClientSignatureConverter.getInstance(CustomStringConverter.INSTANCE), byteArrayInputStream);
        
        Assert.assertEquals(message, recoveredObject.getElement());
    }
    
}
