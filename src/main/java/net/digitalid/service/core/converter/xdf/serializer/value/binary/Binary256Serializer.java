package net.digitalid.service.core.converter.xdf.serializer.value.binary;

import java.math.BigInteger;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.binary.Binary256Wrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.service.core.identity.SemanticType;

public class Binary256Serializer implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) throws StoringException {
        assert (object instanceof BigInteger) : "The object is an instance of a BigInteger type.";
        
        BigInteger value = (BigInteger) object;
        
        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return Binary256Wrapper.encodeNullable(semanticType, value);
    }
    
}
