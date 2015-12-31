package net.digitalid.service.core.converter.xdf.serializer.value.binary;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.binary.BinaryWrapper;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.service.core.identity.SemanticType;

public class XDFBinaryConverter implements Serializer<Block> {
    
    @Override
    public Block store(Object object, Format<Block> format, String fieldName, String clazzName) {
        assert (object instanceof Byte[] || object instanceof byte[]) : "The object is an instance of a byte array type.";

        byte[] value;
        if (object instanceof Byte[]) {
            Byte[] byteArray = (Byte[]) object;
            value = new byte[byteArray.length];
            int i = 0;
            for (Byte b : byteArray) {
                value[i] = b;
                i++;
            }
        } else {
            value = (byte[]) object;
        }
        
        SemanticType semanticType = SemanticType.map(fieldName + "." + clazzName + "@core.digitalid.net");
        return BinaryWrapper.encodeNullable(semanticType, value);
    }
    
}
