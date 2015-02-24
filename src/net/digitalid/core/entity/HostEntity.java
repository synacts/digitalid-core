package net.digitalid.core.entity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.interfaces.SQLizable;

/**
 * This interface models a host entity.
 * 
 * @see HostAccount
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface HostEntity extends Entity, Immutable, SQLizable {
    
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
