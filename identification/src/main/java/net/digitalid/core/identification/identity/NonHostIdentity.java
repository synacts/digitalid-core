package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This interface models a non-host identity.
 * 
 * @see NonHostIdentityImplementation
 * @see InternalNonHostIdentity
 */
@Immutable
public interface NonHostIdentity extends Identity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
}
