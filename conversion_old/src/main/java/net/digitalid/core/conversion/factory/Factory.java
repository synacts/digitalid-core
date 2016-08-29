package net.digitalid.core.conversion.factory;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;

import net.digitalid.core.conversion.format.Format;

@Immutable
public abstract class Factory<O, E> {
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    // TODO: Replace String with some identifier.
    private final @Nonnull String name;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    protected Factory(@Nonnull String name) {
        this.name = name;
    }
    
    /* -------------------------------------------------- Consumption -------------------------------------------------- */
    
    public abstract <R> R consume(@Nonnull O object, @NonCaptured @Nonnull Format<R> format) throws FailedValueStoringException, InternalException;
    
    /* -------------------------------------------------- Production -------------------------------------------------- */
    
    // TODO.
    
}
