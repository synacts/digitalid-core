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
