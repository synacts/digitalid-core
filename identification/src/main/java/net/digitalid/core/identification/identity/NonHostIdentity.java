package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This interface models a non-host identity.
 * 
 * @see InternalNonHostIdentity
 * @see RelocatableIdentity
 */
@Mutable
public interface NonHostIdentity extends Identity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull NonHostIdentifier getAddress();
    
}
