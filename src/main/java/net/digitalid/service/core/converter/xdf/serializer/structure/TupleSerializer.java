package net.digitalid.service.core.converter.xdf.serializer.structure;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.utility.system.converter.exceptions.StoringException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;

public class TupleSerializer implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) throws StoringException {
        assert (object instanceof ReadOnlyArray) : "The object is an instance of a readonly array type.";
        
        ReadOnlyArray<Block> elements = (ReadOnlyArray<Block>) object;

        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return TupleWrapper.encode(semanticType, elements);
    }
    
}
