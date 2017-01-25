package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.InternalIdentifier;

/**
 * This interface models an internal identity.
 * 
 * @see HostIdentity
 * @see InternalNonHostIdentity
 */
@Mutable
public interface InternalIdentity extends Identity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull InternalIdentifier getAddress();
    
}
