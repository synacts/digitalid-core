package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.core.identity.HostIdentity;

import net.digitalid.core.host.Host;

/**
 * This interface models a host entity.
 * 
 * @see HostAccount
 */
@Immutable
public interface HostEntity extends Entity {
    
    /**
     * Returns the host of this entity.
     * 
     * @return the host of this entity.
     */
    @Pure
    public @Nonnull Host getHost();
    
    @Pure
    @Override
    public @Nonnull HostIdentity getIdentity();
    
}
