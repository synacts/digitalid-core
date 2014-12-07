package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import javax.annotation.Nonnull;

/**
 * This interface models a host entity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface HostEntity extends Entity, Immutable, SQLizable {
    
    @Pure
    @Override
    public abstract @Nonnull HostIdentity getIdentity();
    
}
