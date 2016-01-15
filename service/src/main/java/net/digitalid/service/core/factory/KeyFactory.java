package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;

@Immutable
public abstract class KeyFactory<O, E> extends Factory<O, E> {
    
    protected KeyFactory(@Nonnull String name) {
        super(name);
    }
    
}
