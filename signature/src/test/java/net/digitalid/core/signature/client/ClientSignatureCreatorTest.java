package net.digitalid.core.signature.client;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.commitment.SecretCommitment;
import net.digitalid.core.commitment.SecretCommitmentBuilder;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.signature.host.HostSignature;
import net.digitalid.core.signature.host.HostSignatureCreator;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

/**
 *
 */
public class ClientSignatureCreatorTest extends CoreTest {
    
    @Test
    public void shouldSignAndCreateClientSignature() throws ExternalException {
        final @Nonnull String message = "This is a secret message";
        final @Nonnull HostIdentifier hostIdentifier = HostIdentifier.with("digitalid.net");
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        
        final @Nonnull Time time = TimeBuilder.buildWithValue(1472810684033L);
        final @Nonnull HostIdentity hostIdentity = hostIdentifier.resolve();
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(hostIdentity, time);
        
        // TODO: what to chose as value and secret?
//        final @Nonnull BigInteger value = new BigInteger("4");
        final @Nonnull Exponent secret = ExponentBuilder.withValue(BigInteger.TEN).build();
        
        final @Nonnull SecretCommitment secretCommitment = SecretCommitmentBuilder.withHost(hostIdentity).withTime(time).withPublicKey(publicKey).withSecret(secret).build();
        
        final @Nonnull ClientSignature<String> signedMessage = ClientSignatureCreator.sign(message, StringConverter.INSTANCE).to(subject).with(secretCommitment);
        signedMessage.verifySignature();
        
//        final @Nonnull byte[] bytes = XDF.convert(ClientSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedMessage);
//        assertThat(bytes.length).isPositive();
//        
//        final @Nonnull ClientSignature<String> recoveredObject = XDF.recover(ClientSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
//        assertThat(recoveredObject.getObject()).isEqualTo(message);
    }
    
}
