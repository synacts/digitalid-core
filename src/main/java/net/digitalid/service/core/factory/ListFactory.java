package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;

@Immutable
public abstract class ListFactory<O, E> extends Factory<O, E> {
    
    protected ListFactory(@Nonnull String name) {
        super(name);
    }
    
}
