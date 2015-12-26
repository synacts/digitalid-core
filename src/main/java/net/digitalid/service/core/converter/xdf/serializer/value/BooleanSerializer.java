package net.digitalid.service.core.converter.xdf.serializer.value;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.BooleanWrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.service.core.identity.SemanticType;

public class BooleanSerializer implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) {
        assert (object instanceof Boolean) : "The object is an instance of the boolean type.";

        Boolean value = (Boolean) object;
        
        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return BooleanWrapper.encode(semanticType, value);
    }
    
}
