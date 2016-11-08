package net.digitalid.core.handler.method.query;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.method.Method;

/**
 * External queries can be sent by both {@link Host hosts} and {@link Client clients}.
 */
@Immutable
public abstract class ExternalQuery<E extends Entity> extends Query<E> {
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof ExternalQuery;
    }
    
}
