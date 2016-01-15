package net.digitalid.service.core.factory;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.reference.NonCapturable;
import net.digitalid.utility.validation.state.Immutable;

import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;

import net.digitalid.service.core.format.Format;

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
    
    public abstract <R> R consume(@Nonnull O object, @NonCapturable @Nonnull Format<R> format) throws FailedValueStoringException, InternalException;
    
    /* -------------------------------------------------- Production -------------------------------------------------- */
    
    // TODO.
    
}
