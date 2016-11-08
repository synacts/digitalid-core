package net.digitalid.core.handler.method.query;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.InternalMethod;
import net.digitalid.core.handler.method.Method;

/**
 * Internal queries can only be sent by {@link Client clients} and are always signed identity-based.
 */
@Immutable
public abstract class InternalQuery extends Query<NonHostEntity> implements InternalMethod {
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return super.isSimilarTo(other) && other instanceof InternalQuery;
    }
    
}
