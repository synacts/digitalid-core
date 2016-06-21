package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.resolution.Mapper;

/**
 * This interface models an internal person.
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 */
@Immutable
public interface InternalPerson extends Person implements InternalNonHostIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Stores the presumable address of this internal person.
     * The address is updated when the person is relocated or merged.
     */
    private @Nonnull InternalNonHostIdentifier address;
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentifier getAddress() {
        return address;
    }
    
    @Impure
    @Override
    public final void setAddress(@Nonnull Mapper.Key key, @Nonnull InternalNonHostIdentifier address) {
        key.hashCode();
        this.address = address;
    }
    
}
