package net.digitalid.core.signature.host;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.exceptions.SignatureException;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class HostSignatureCreatorTest extends CoreTest {
    
    @Test
    public void shouldSignAndCreateHostSignature() throws RecoveryException, SignatureException {
        final @Nonnull String message = "This is an authentic message.";
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        final @Nonnull InternalIdentifier signer = InternalIdentifier.with("alice@digitalid.net");
        
        final @Nonnull HostSignature<@Nonnull String> signedIdentifier = HostSignatureCreator.sign(message, StringConverter.INSTANCE).about(subject).as(signer);
        
        final @Nonnull PublicKey publicKey;
        try {
            publicKey = PublicKeyRetriever.retrieve(signer.getHostIdentifier(), TimeBuilder.build());
        } catch (@Nonnull ExternalException exception) {
            throw RecoveryExceptionBuilder.withMessage(Strings.format("Could not retrieve the public key of $.", signer.getHostIdentifier())).withCause(exception).build();
        }
        signedIdentifier.verifySignature(publicKey);
    }
    
}
