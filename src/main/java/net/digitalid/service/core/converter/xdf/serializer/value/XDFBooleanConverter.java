package net.digitalid.service.core.converter.xdf.serializer.value;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.value.BooleanWrapper;
import net.digitalid.service.core.converter.xdf.XDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.system.converter.exceptions.StoringException;

public class XDFBooleanConverter extends XDFConverter<Boolean> {

    @Override
    public Block store(Object object, Class<?> type, String fieldName, String parentName) throws StoringException {
        assert object == null || object instanceof Boolean : "The object is of type boolean.";
        
        Boolean value = (Boolean) object;
        SemanticType semanticType = SemanticType.map(fieldName + "." + parentName + "@core.digitalid.net");
        return BooleanWrapper.encode(semanticType, value);
    }
    
}
