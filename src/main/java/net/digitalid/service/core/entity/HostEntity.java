package net.digitalid.service.core.entity;

import net.digitalid.service.core.site.host.Host;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

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
