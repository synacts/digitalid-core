package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import java.math.BigInteger;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.IntegerWrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.service.core.identity.SemanticType;

public class XDFIntegerConverter implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) {
        assert (object instanceof BigInteger) : "The object is an instance of the BigInteger type.";

        BigInteger value = (BigInteger) object;
        
        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return IntegerWrapper.encodeNullable(semanticType, value);
    }
    
}
