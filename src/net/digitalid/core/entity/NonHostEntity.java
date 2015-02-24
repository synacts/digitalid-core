package net.digitalid.core.entity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.interfaces.SQLizable;

/**
 * This interface models a non-host entity.
 * 
 * @see NonHostAccount
 * @see Role
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface NonHostEntity extends Entity, Immutable, SQLizable {
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}
