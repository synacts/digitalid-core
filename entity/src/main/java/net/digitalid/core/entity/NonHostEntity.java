package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identity.InternalNonHostIdentity;

/**
 * This interface models a non-host entity.
 * 
 * @see NonHostAccount
 * @see Role
 */
@Immutable
public interface NonHostEntity extends Entity<NonHostEntity> {
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}
