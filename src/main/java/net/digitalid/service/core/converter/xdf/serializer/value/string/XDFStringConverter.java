package net.digitalid.service.core.converter.xdf.serializer.value.string;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.system.converter.exceptions.StoringException;

public class XDFStringConverter extends XDFConverter<String> {

    @Override
    public Block store(Object object, Class<?> type, String fieldName, String parentName) throws StoringException {
        assert (object == null || object instanceof Character) : "The object is an instance of a character type.";
        
        String value = (String) object;

        SemanticType semanticType = SemanticType.map(fieldName + "." + parentName + "@core.digitalid.net");
        return StringWrapper.encodeNullable(semanticType, value);       
    }
    
}

