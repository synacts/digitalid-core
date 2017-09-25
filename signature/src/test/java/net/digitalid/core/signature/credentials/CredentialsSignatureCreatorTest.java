package net.digitalid.core.signature.credentials;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.commitment.SecretCommitmentBuilder;
import net.digitalid.core.credential.ClientCredentialBuilder;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.signature.client.ClientSignature;
import net.digitalid.core.signature.client.ClientSignatureCreator;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

/**
 *
 */
public class CredentialsSignatureCreatorTest extends CoreTest {
    
    @Test
    public void shouldSignAndCreateCredentialsSignature() throws ExternalException {
        final @Nonnull String message = "This is a secret message";
        final @Nonnull HostIdentifier hostIdentifier = HostIdentifier.with("digitalid.net");
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        
        final @Nonnull Time time = TimeBuilder.buildWithValue(1472810684033L);
        final @Nonnull HostIdentity hostIdentity = hostIdentifier.resolve();
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(hostIdentity, time);
        
//        ClientCredentialBuilder.withExposedExponent().withC().withE().withB().withU().withV().withI().build();
        
//        final @Nonnull ClientSignature<String> signedMessage = CredentialsSignatureCreator.sign(message, StringConverter.INSTANCE).to(subject).lodged(false).with(clientCredential);
//        signedMessage.verifySignature();
        
//        final @Nonnull byte[] bytes = XDF.convert(ClientSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedMessage);
//        assertThat(bytes.length).isPositive();
//        
//        final @Nonnull ClientSignature<String> recoveredObject = XDF.recover(ClientSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
//        assertThat(recoveredObject.getObject()).isEqualTo(message);
    }
    
}
