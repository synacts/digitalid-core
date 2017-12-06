/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
