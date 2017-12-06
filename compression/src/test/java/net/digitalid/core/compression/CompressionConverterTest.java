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
package net.digitalid.core.compression;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;

import net.digitalid.core.conversion.XDF;

import org.junit.Assert;
import org.junit.Test;

public class CompressionConverterTest {
    
    @Test
    public void shouldConvertCompressedObject() throws Exception {
        final @Nonnull String string = "user.user@digitalid.net";
        final @Nonnull Compression<String> compressedString = CompressionBuilder.withObject(string).build();
        final @Nonnull byte[] compressedBytes = XDF.convert(CompressionConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), compressedString);
        final @Nonnull Compression<String> decompressedString = XDF.recover(CompressionConverterBuilder.withObjectConverter(StringConverter.INSTANCE).build(), null, compressedBytes);
        Assert.assertEquals(string, decompressedString.getObject());
    }
    
}
