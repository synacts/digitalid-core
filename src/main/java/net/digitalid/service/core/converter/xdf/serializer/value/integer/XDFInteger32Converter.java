package net.digitalid.service.core.converter.xdf.serializer.value.integer;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.service.core.identity.SemanticType;

public class XDFInteger32Converter implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) {
        assert (object instanceof Integer) : "The object is an instance of the integer type.";

        int value = (Integer) object;
        
        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return Integer64Wrapper.encode(semanticType, value);
    }
    
}
