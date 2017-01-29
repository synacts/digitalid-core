package net.digitalid.core.signature.client;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.commitment.SecretCommitmentBuilder;
import net.digitalid.core.conversion.XDF;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.IdentifierResolver;

import org.junit.Assert;
import org.junit.Test;

public class ClientSignatureConverterTest extends CryptographyTestBase {
    
    @Test
    public void shouldSignAndVerify() throws Exception {
        final @Nonnull String message = "This is a secret message";
        final @Nonnull HostIdentifier hostIdentifier = HostIdentifier.with("digitalid.net");
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        
        final @Nonnull Time time = TimeBuilder.buildWithValue(1472810684033L);
        final @Nonnull HostIdentity hostIdentity = (HostIdentity) IdentifierResolver.resolve(hostIdentifier);
        final @Nonnull PublicKey publicKey = publicKeyRetrieverForTest.getPublicKey(hostIdentity, time);
        
        // TODO: what to chose as value and secret?
//        final @Nonnull BigInteger value = new BigInteger("4");
        final @Nonnull Exponent secret = ExponentBuilder.withValue(BigInteger.TEN).build();
        
        final @Nonnull SecretCommitment secretCommitment = SecretCommitmentBuilder.withHost(hostIdentity).withTime(time).withPublicKey(publicKey).withSecret(secret).build();
        
        final @Nonnull ClientSignature<String> signedMessage = ClientSignatureBuilder.withObject(message).withSubject(subject).withCommitment(secretCommitment).withTime(time).build(); 
        
        final @Nonnull byte[] bytes = XDF.convert(ClientSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedMessage);
        Assert.assertTrue(bytes.length > 0);
        
        final @Nonnull ClientSignature<String> recoveredObject = XDF.recover(ClientSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
        assertEquals(message, recoveredObject.getObject());
    }
    
}
