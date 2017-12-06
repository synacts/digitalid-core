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

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class HostSignatureConverterTest extends CoreTest {
    
    @Test
    public void shouldConvertCorrectly() throws RecoveryException {
        final @Nonnull String message = "This is an authentic message.";
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        final @Nonnull InternalIdentifier signer = InternalIdentifier.with("alice@digitalid.net");
        
        final @Nonnull HostSignature<@Nonnull String> signedIdentifier = HostSignatureBuilder.withObjectConverter(StringConverter.INSTANCE).withObject(message).withSubject(subject).withSigner(signer).withSignatureValue(BigInteger.ONE).build();
        
        final @Nonnull byte[] bytes = XDF.convert(HostSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedIdentifier);
        assertThat(bytes.length).isPositive();
        
        final @Nonnull HostSignature<String> recoveredObject = XDF.recover(HostSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
        assertThat(recoveredObject.getObject()).isEqualTo(message);
    }
    
}
