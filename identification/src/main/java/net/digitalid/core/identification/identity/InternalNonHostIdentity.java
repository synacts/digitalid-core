package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;

/**
 * This interface models an internal non-host identity.
 * 
 * @see Type
 * @see InternalPerson
 */
@Mutable
@GenerateConverter
public interface InternalNonHostIdentity extends InternalIdentity, NonHostIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentifier getAddress();
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns the identity of the given address.
     */
    @Pure
    @Recover // TODO: Split into @Recover(Representation.INTERNAL) and @Recover(Representation.EXTERNAL) or something similar.
    static @Nonnull InternalNonHostIdentity with(long key, @Nonnull InternalNonHostIdentifier address) {
        try {
            // TODO: The following cast should probably not throw an internal exception.
            return IdentifierResolver.resolve(address).castTo(InternalNonHostIdentity.class);
        } catch (@Nonnull ExternalException exception) {
            // TODO: How to handle this?
            throw new RuntimeException(exception);
        }
    }
    
}
