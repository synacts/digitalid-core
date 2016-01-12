package net.digitalid.service.core.format.xdf;

import javax.annotation.Nonnull;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.factory.Tuple2Factory;
import net.digitalid.service.core.format.Tuple2Format;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.exceptions.internal.InternalException;

public class XDFTuple2Format<O1, E1, O2, E2> implements Tuple2Format<Block, O1, E1, O2, E2> {
    
    private final @Nonnull Tuple2Factory<?, ?, O1, E1, O2, E2> factory;
    
    private final @Nonnull FreezableArray<Block> elements;
    
    @Override
    public void consume1(O1 object) throws FailedValueStoringException, InternalException {
        if (object != null || factory.factory1.isNullable()) { throw InternalException.get("Problem!"); }
        
        elements.set(0, factory.factory1.consume(object, this));
    }
    
    @Override
    public void consume2(O2 object) throws FailedValueStoringException, InternalException {
        elements.set(1, factory.factory2.consume(object, this));
    }
    
    // or:
    
    public void consume(@Nonnull Pair<O1, O2> pair) {
        elements.set(0, factory.factory1.consume(pair.get0(), this));
        elements.set(1, factory.factory2.consume(pair.get1(), this));
    }
    
    /* -------------------------------------------------- Production -------------------------------------------------- */
    
    @Override
    public O1 produce1(@Nonnull E1 external) {
        return factory.factory1.produce(external, elements.getNullable(0));
    }
    
    @Override
    public @Nonnull Block finish() {
        return TupleWrapper.encode(generateType(), elements.freeze());
    }
    
    public XDFTuple2Format(@Nonnull Tuple2Factory<?, ?, O1, E1, O2, E2> factory) {
        this.factory = factory;
        this.elements = FreezableArray.get(2);
    }
    
}
