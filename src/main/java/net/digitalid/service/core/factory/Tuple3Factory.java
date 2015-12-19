package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;

@Immutable
public abstract class Tuple3Factory<O, E, O1, E1, O2, E2, O3, E3> extends Tuple2Factory<O, E, O1, E1, O2, E2> {
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    public final @Nonnull Factory<O3, E3> factory3;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected Tuple3Factory(@Nonnull String name, @Nonnull Factory<O1, E1> factory1, @Nonnull Factory<O2, E2> factory2, @Nonnull Factory<O3, E3> factory3) {
        super(name, factory1, factory2);
        
        this.factory3 = factory3;
    }
    
}
