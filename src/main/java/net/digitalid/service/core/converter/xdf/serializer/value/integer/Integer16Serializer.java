package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.Integer16Wrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.service.core.identity.SemanticType;

public class Integer16Serializer implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) {
        assert (object instanceof Short) : "The object is an instance of the byte type.";

        short value = (Short) object;
        
        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return Integer16Wrapper.encode(semanticType, value);
    }
    
}
