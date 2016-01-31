package net.digitalid.core.conversion.factory;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;

@Immutable
public abstract class KeyFactory<O, E> extends Factory<O, E> {
    
    protected KeyFactory(@Nonnull String name) {
        super(name);
    }
    
}
