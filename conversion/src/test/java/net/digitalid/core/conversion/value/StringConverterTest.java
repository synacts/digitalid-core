package net.digitalid.core.conversion.value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.core.conversion.XDF;
import net.digitalid.core.conversion.value.testentities.CustomString;
import net.digitalid.core.conversion.value.testentities.CustomStringBuilder;
import net.digitalid.core.conversion.value.testentities.CustomStringConverter;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class StringConverterTest {
    
    @Test
    public void shouldConvertString() throws Exception {
        final CustomString string = CustomStringBuilder.withValue("Simple test").build();
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XDF.convert(string, CustomStringConverter.INSTANCE, outputStream);
        
        final @Nonnull byte[] incomingBytes = outputStream.toByteArray();
        
        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(incomingBytes);
        final @Nullable CustomString recoveredString = XDF.recover(CustomStringConverter.INSTANCE, null, inputStream);
    
        Assert.assertEquals(string, recoveredString);
    }
    
}
