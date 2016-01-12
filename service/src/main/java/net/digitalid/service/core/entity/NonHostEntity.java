package net.digitalid.service.core.entity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

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
