package net.digitalid.core.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.converters.StringConverter;

import net.digitalid.core.conversion.XDF;

import org.junit.Assert;
import org.junit.Test;

public class CompressionConverterTest {
    
    @Test
    public void shouldConvertCompressedObject() throws Exception {
        final @Nonnull String string = "user.user@digitalid.net";
        final @Nonnull Compression<String> compressedIdentifier = CompressionBuilder.withObject(string).build();
        @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(compressedIdentifier, CompressionConverter.getInstance(StringConverter.INSTANCE), byteArrayOutputStream);
    
        final byte[] convertedBytes = byteArrayOutputStream.toByteArray();
        
        Assert.assertTrue("The identifier address was not compressed (length of compressed identifier: " + convertedBytes.length + ", length of uncompressed identifier: " + string.getBytes("UTF-16BE").length + ")", convertedBytes.length < string.getBytes("UTF-16BE").length);
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(convertedBytes);
        final @Nullable Compression<String> decodedIdentifier = XDF.recover(CompressionConverter.getInstance(StringConverter.INSTANCE), null, byteArrayInputStream);
    
        Assert.assertNotNull(decodedIdentifier);
        Assert.assertEquals(string, decodedIdentifier.getObject());
    }
    
}
