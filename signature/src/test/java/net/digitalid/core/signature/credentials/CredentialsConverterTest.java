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
package net.digitalid.core.signature.credentials;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.ElementBuilder;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.group.GroupWithUnknownOrderBuilder;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.signature.attribute.CertifiedAttributeValue;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CredentialsConverterTest {
    
    @Test
    public void shouldConvert() throws Exception {
        final @Nonnull String message = "This is a secret message";
        final @Nonnull InternalIdentifier subject = InternalIdentifier.with("bob@digitalid.net");
        
        final @Nonnull Time time = TimeBuilder.buildWithValue(1472810684033L);
        
        // TODO: find better test values
        final @Nonnull Exponent t = ExponentBuilder.withValue(BigInteger.ONE).build();
        final @Nonnull Exponent su = ExponentBuilder.withValue(BigInteger.valueOf(2)).build();
        final @Nonnull FreezableArrayList<PublicClientCredential> credentials = FreezableArrayList.withNoElements();
        final @Nonnull FreezableArrayList<CertifiedAttributeValue> certificates = FreezableArrayList.withNoElements();
        final @Nonnull Exponent sv = ExponentBuilder.withValue(BigInteger.valueOf(3)).build();
        final @Nonnull Element fPrime = ElementBuilder.withGroup(GroupWithUnknownOrderBuilder.withModulus(BigInteger.valueOf(1)).build()).withValue(BigInteger.ONE).build();
        final @Nonnull Exponent sbPrime = ExponentBuilder.withValue(BigInteger.valueOf(5)).build();
        final @Nonnull CredentialsSignature<String> signedMessage = CredentialsSignatureBuilder.withObjectConverter(StringConverter.INSTANCE).withObject(message).withSubject(subject).withT(t).withSU(su).withCredentials(credentials).withCertificates(certificates).withSV(sv).withFPrime(fPrime).withSBPrime(sbPrime).withTime(time).build();

        final @Nonnull byte[] bytes = XDF.convert(CredentialsSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), signedMessage);
        assertThat(bytes.length).isPositive();

        final @Nonnull CredentialsSignature<String> recoveredObject = XDF.recover(CredentialsSignatureConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, bytes);
        assertThat(recoveredObject.getObject()).isEqualTo(message);
    }
    
}
