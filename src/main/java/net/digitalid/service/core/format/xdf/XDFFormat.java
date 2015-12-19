package net.digitalid.service.core.format.xdf;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.factory.Tuple2Factory;
import net.digitalid.service.core.format.Format;

public class XDFFormat implements Format<Block> {
    
//    public @Nonnull KeyFormat getKeyFormat();
    
//    public @Nonnull ListFormat getListFormat();
    
    @Override
    public @Nonnull <O1, E1, O2, E2> XDFTuple2Format<O1, E1, O2, E2> getTuple2Format(@Nonnull Tuple2Factory<?, ?, O1, E1, O2, E2> factory) {
        return new XDFTuple2Format(factory);
    }
    
//    public @Nonnull Tuple3Format getTuple3Format();
    
}
