package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This class models an identity that can be relocated.
 * 
 * @see Type
 * @see Person
 */
@Mutable
@GenerateConverter
public abstract class RelocatableIdentity implements NonHostIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Sets the address of this relocatable identity.
     */
    @Impure
    abstract void setAddress(@Nonnull InternalNonHostIdentifier address);
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns the identity of the given address.
     */
    @Pure
    @Recover // TODO: Split into @Recover(Representation.INTERNAL) and @Recover(Representation.EXTERNAL) or something similar.
    static @Nonnull RelocatableIdentity with(long key, @Nonnull NonHostIdentifier address) {
        try {
            // TODO: The following cast should probably not throw an internal exception.
            return IdentifierResolver.resolve(address).castTo(RelocatableIdentity.class);
        } catch (@Nonnull ExternalException exception) {
            // TODO: How to handle this?
            throw new RuntimeException(exception);
        }
    }
    
}
