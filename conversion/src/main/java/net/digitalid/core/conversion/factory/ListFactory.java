package net.digitalid.core.conversion.factory;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;

@Immutable
public abstract class ListFactory<O, E> extends Factory<O, E> {
    
    protected ListFactory(@Nonnull String name) {
        super(name);
    }
    
}
