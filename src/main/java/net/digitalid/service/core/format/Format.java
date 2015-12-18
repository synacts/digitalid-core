package net.digitalid.service.core.format;

import javax.annotation.Nonnull;
import net.digitalid.service.core.factory.Tuple2Factory;

/**
 * SQL, XDF and so on are all formats.
 */
public interface Format<R> {
    
//    public @Nonnull KeyFormat getKeyFormat();
    
//    public @Nonnull ListFormat getListFormat();
    
    public @Nonnull <O1, E1, O2, E2> Tuple2Format<R, O1, E1, O2, E2> getTuple2Format(@Nonnull Tuple2Factory<?, ?, O1, E1, O2, E2> factory);
    
//    public @Nonnull Tuple3Format getTuple3Format();
    
}
