package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identity.InternalNonHostIdentity;

/**
 * This interface models a non-host entity.
 */
@Immutable
// TODO: @GenerateConverter
public interface NonHostEntity extends Entity {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}
