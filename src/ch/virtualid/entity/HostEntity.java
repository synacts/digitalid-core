package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Host;
import javax.annotation.Nonnull;

/**
 * This interface models a host entity.
 * 
 * @see HostAccount
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
