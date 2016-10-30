package net.digitalid.core.conversion.transformational;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionConverter;
import net.digitalid.core.conversion.converter.XDF;
import net.digitalid.core.cryptography.compression.CompressionBuilder;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.IdentifierConverter;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CompressionConverterTest {
    
    @Test
    public void shouldConvertCompressedObject() throws Exception {
        final @Nonnull Identifier identifier = Identifier.with("annnnna@digitalid.net");
        final @Nonnull Compression<Identifier> compressedIdentifier = CompressionBuilder.withObject(identifier).build();
        @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(compressedIdentifier, CompressionConverter.getInstance(IdentifierConverter.INSTANCE), byteArrayOutputStream);
    
        final byte[] convertedBytes = byteArrayOutputStream.toByteArray();
        
        Assert.assertTrue("The identifier address was not compressed (length of compressed identifier: " + convertedBytes.length + ", length of uncompressed identifier: " + identifier.getString().getBytes("UTF-16BE").length + ")", convertedBytes.length < identifier.getString().getBytes("UTF-16BE").length);
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(convertedBytes);
        final @Nullable Compression<Identifier> decodedIdentifier = XDF.recover(CompressionConverter.getInstance(IdentifierConverter.INSTANCE), byteArrayInputStream);
    
        Assert.assertNotNull(decodedIdentifier);
        Assert.assertEquals(identifier, decodedIdentifier.getObject());
    }
    
}
