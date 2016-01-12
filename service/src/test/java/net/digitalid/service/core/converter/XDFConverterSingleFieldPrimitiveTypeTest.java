package net.digitalid.service.core.converter;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.serializer.value.XDFBooleanConverter;
import org.junit.Assert;
import org.junit.Test;

public class XDFConverterSingleFieldPrimitiveTypeTest {
    
    @Test
    public void shouldConvertBoolean() throws Exception {
        boolean var = true;
        XDFBooleanConverter booleanXDFConverter = new XDFBooleanConverter();
        Block block = booleanXDFConverter.convertTo(var, boolean.class, "var", null);

        Assert.assertNotNull(block);
        // TODO: Design test framework for testing whether a block was correctly encoded.
    }
    
    @Test
    public void shouldConvertByte() throws Exception {}

    @Test
    public void shouldConvertShort() throws Exception {}

    @Test
    public void shouldConvertInteger() throws Exception {}

    @Test
    public void shouldConvertLong() throws Exception {}

    @Test
    public void shouldConvertBigInteger() throws Exception {}

    @Test
    public void shouldConvertByteArray() throws Exception {}

    @Test
    public void shouldConvertCharacter() throws Exception {}

    @Test
    public void shouldConvertString() throws Exception {}

}
