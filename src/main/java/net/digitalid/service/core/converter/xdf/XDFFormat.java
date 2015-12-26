package net.digitalid.service.core.converter.xdf;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.utility.system.converter.Deserializer;
import net.digitalid.utility.system.converter.Format;
import net.digitalid.utility.system.converter.List;
import net.digitalid.utility.system.converter.Serializer;
import net.digitalid.utility.system.converter.Tuple;
import net.digitalid.utility.system.converter.exceptions.SerializerException;
import net.digitalid.service.core.converter.xdf.serializer.structure.ListSerializer;
import net.digitalid.service.core.converter.xdf.serializer.value.BooleanSerializer;
import net.digitalid.service.core.converter.xdf.serializer.value.binary.BinarySerializer;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.Integer08Serializer;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.Integer16Serializer;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.Integer32Serializer;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.Integer64Serializer;
import net.digitalid.service.core.converter.xdf.serializer.value.ObjectSerializer;
import net.digitalid.service.core.converter.xdf.serializer.structure.TupleSerializer;
import net.digitalid.service.core.converter.xdf.serializer.value.integer.IntegerSerializer;
import net.digitalid.service.core.converter.xdf.serializer.value.string.CharacterSerializer;
import net.digitalid.service.core.converter.xdf.serializer.value.string.StringSerializer;
import net.digitalid.utility.collections.freezable.FreezableHashMap;
import net.digitalid.utility.collections.freezable.FreezableMap;

public class XDFFormat implements Format<Block> {
    
    private static final FreezableMap<Class<?>, Serializer> typeSerializers = FreezableHashMap.get();
    private static final Serializer<Block> defaultSerializer = new ObjectSerializer();
    
    static {
        typeSerializers.put(boolean.class, new BooleanSerializer());
        typeSerializers.put(Boolean.class, new BooleanSerializer());
        
        typeSerializers.put(byte.class, new Integer08Serializer());
        typeSerializers.put(Byte.class, new Integer08Serializer());
        typeSerializers.put(short.class, new Integer16Serializer());
        typeSerializers.put(Short.class, new Integer16Serializer());
        typeSerializers.put(int.class, new Integer32Serializer());
        typeSerializers.put(Integer.class, new Integer32Serializer());
        typeSerializers.put(long.class, new Integer64Serializer());
        typeSerializers.put(Long.class, new Integer64Serializer());
        typeSerializers.put(BigInteger.class, new IntegerSerializer());
        
        typeSerializers.put(char.class, new CharacterSerializer());
        typeSerializers.put(Character.class, new CharacterSerializer());
        typeSerializers.put(String.class, new StringSerializer());
        
        typeSerializers.put(byte[].class, new BinarySerializer());
        typeSerializers.put(Byte[].class, new BinarySerializer());
        
        typeSerializers.put(Tuple.class, new TupleSerializer());
        typeSerializers.put(List.class, new ListSerializer());
    }
    
    @Override
    public @Nonnull <T> Serializer<Block> getSerializer(Class<T> clazz) throws SerializerException {
        @Nullable Serializer<Block> blockSerializer = typeSerializers.get(clazz); 
        if (blockSerializer == null) {
            blockSerializer = defaultSerializer;
        }
        return blockSerializer;
    }

    @Override
    public <T> Deserializer<Block, T> getDeserializer(Block encodedForm) {
        return null;
    }
}
