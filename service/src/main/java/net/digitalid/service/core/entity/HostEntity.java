package net.digitalid.service.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.site.host.Host;

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
