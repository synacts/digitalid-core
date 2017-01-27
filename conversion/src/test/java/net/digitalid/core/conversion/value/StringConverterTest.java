package net.digitalid.core.conversion.value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;

import org.junit.Test;

public class StringConverterTest {
    
    @Test
    public void shouldConvertString() throws Exception {
        final @Nonnull String string = "Hello World!";
        final @Nonnull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        XDF.convert(string, StringConverter.INSTANCE, outputStream);
        
        final @Nonnull byte[] incomingBytes = outputStream.toByteArray();
        
        final @Nonnull ByteArrayInputStream inputStream = new ByteArrayInputStream(incomingBytes);
//        final @Nonnull String recoveredString = XDF.recover(StringConverter.INSTANCE, null, inputStream);
    
//        Assert.assertEquals(string, recoveredString);
    }
    
}
