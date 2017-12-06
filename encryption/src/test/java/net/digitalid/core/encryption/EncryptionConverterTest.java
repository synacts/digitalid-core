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
package net.digitalid.core.encryption;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.InitializationVector;
import net.digitalid.core.symmetrickey.InitializationVectorBuilder;
import net.digitalid.core.symmetrickey.InitializationVectorConverter;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class EncryptionConverterTest extends CoreTest {
    
    @Pure
    public <@Unspecifiable TYPE> void assertEncryption(@Nonnull Converter<TYPE, Void> converter, @NonCaptured @Unmodified @Nonnull TYPE object) throws ExternalException {
        final @Nonnull HostIdentifier recipient = HostIdentifier.with("digitalid.net");
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull SymmetricKey symmetricKey = SymmetricKeyBuilder.build();
        final @Nonnull InitializationVector initializationVector = InitializationVectorBuilder.build();
        
        final @Nonnull RequestEncryption<TYPE> encryption = RequestEncryptionBuilder.withObject(object).withRecipient(recipient).withTime(time).withSymmetricKey(symmetricKey).withInitializationVector(initializationVector).build();
        final @Nonnull RequestEncryptionConverter<TYPE> encryptionConverter = RequestEncryptionConverterBuilder.withObjectConverter(converter).build();
        final @Nonnull byte[] bytes = XDF.convert(encryptionConverter, encryption);
        final @Nonnull RequestEncryption<TYPE> recoveredEncryption = XDF.recover(encryptionConverter, null, bytes);
        
        assertThat(recoveredEncryption.getRecipient()).isEqualTo(recipient);
        assertThat(recoveredEncryption.getSymmetricKey()).isEqualTo(symmetricKey);
        assertThat(recoveredEncryption.getInitializationVector()).isEqualTo(initializationVector);
        assertThat(recoveredEncryption.getObject()).isEqualTo(object);
    }
    
    @Test
    public void testEncryptionConverterWithStrings() throws ExternalException {
        assertEncryption(StringConverter.INSTANCE, "Hello World!");
        assertEncryption(StringConverter.INSTANCE, "Let's look whether it also works with longer messages.");
        assertEncryption(StringConverter.INSTANCE, Strings.repeat("A short sentence. ", 100));
    }
    
    @Test
    public void testEncryptionConverterWithInitializationVector() throws ExternalException {
        assertEncryption(InitializationVectorConverter.INSTANCE, InitializationVectorBuilder.build());
    }
    
}
