package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;

/**
 * This class models an identity that can be relocated.
 * 
 * @see Type
 * @see Person
 */
@Mutable
public abstract class RelocatableIdentity implements NonHostIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Sets the address of this relocatable identity.
     */
    @Impure
    abstract void setAddress(@Nonnull InternalNonHostIdentifier address);
    
}
