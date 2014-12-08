package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import javax.annotation.Nonnull;

/**
 * This interface models a non-host entity.
 * 
 * @see NonHostAccount
 * @see Role
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface NonHostEntity extends Entity, Immutable, SQLizable {
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}
