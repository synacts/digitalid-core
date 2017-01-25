package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;

/**
 * This interface models an internal non-host identity.
 * 
 * @see Type
 * @see InternalPerson
 */
@Mutable
public interface InternalNonHostIdentity extends InternalIdentity, NonHostIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull InternalNonHostIdentifier getAddress();
    
}
