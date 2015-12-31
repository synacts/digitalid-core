package net.digitalid.service.core.converter.xdf.serializer.value.string;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.string.String01Wrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.service.core.identity.SemanticType;

public class XDFCharacterConverter implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) throws StoringException {
        assert (object instanceof Character) : "The object is an instance of a character type.";
        
        Character character = (Character) object;

        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return String01Wrapper.encode(semanticType, character);
    }
    
}
