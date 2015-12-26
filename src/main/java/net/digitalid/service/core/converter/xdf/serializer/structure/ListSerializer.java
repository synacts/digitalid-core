package net.digitalid.service.core.converter.xdf.serializer.structure;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.collections.readonly.ReadOnlyList;

public class ListSerializer implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) throws StoringException {
        assert (object instanceof ReadOnlyList) : "The object is an instance of a readonly list type.";
        
        ReadOnlyList<Block> elements = (ReadOnlyList<Block>) object;

        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return ListWrapper.encode(semanticType, elements);
    }
    
}
