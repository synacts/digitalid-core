package net.digitalid.core.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootInterface;

import net.digitalid.core.conversion.XDF;

import org.junit.Assert;
import org.junit.Test;

@GenerateBuilder
@GenerateSubclass
@GenerateConverter
interface TestString extends RootInterface {
    
    @Pure
    public @Nonnull String getString();
    
}

/**
 *
 */
public class CompressionConverterTest {
    
    @Test
    public void shouldConvertCompressedObject() throws Exception {
        final @Nonnull TestString string = TestStringBuilder.withString("user.user@digitalid.net").build();
        final @Nonnull Compression<TestString> compressedIdentifier = CompressionBuilder.withObject(string).build();
        @Nonnull ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XDF.convert(compressedIdentifier, CompressionConverter.getInstance(TestStringConverter.INSTANCE), byteArrayOutputStream);
    
        final byte[] convertedBytes = byteArrayOutputStream.toByteArray();
        
        Assert.assertTrue("The identifier address was not compressed (length of compressed identifier: " + convertedBytes.length + ", length of uncompressed identifier: " + string.getString().getBytes("UTF-16BE").length + ")", convertedBytes.length < string.getString().getBytes("UTF-16BE").length);
        
        final @Nonnull ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(convertedBytes);
        final @Nullable Compression<TestString> decodedIdentifier = XDF.recover(CompressionConverter.getInstance(TestStringConverter.INSTANCE), null, byteArrayInputStream);
    
        Assert.assertNotNull(decodedIdentifier);
        Assert.assertEquals(string, decodedIdentifier.getObject());
    }
    
}
