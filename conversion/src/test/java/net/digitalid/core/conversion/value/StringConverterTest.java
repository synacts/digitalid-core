package net.digitalid.core.conversion.value;

import javax.annotation.Nonnull;

import net.digitalid.utility.conversion.converters.StringConverter;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.testing.RootTest;

import net.digitalid.core.conversion.XDF;

import org.junit.Test;

public class StringConverterTest extends RootTest {
    
    @Test
    public void shouldConvertString() throws RecoveryException {
        final @Nonnull String originalString = "Hello World!";
        final @Nonnull byte[] bytes = XDF.convert(StringConverter.INSTANCE, originalString);
        final @Nonnull String recoveredString = XDF.recover(StringConverter.INSTANCE, null, bytes);
        assertEquals(originalString, recoveredString);
    }
    
}
