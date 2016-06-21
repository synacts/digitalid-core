package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identifier.NonHostIdentifier;
import net.digitalid.core.resolution.Mapper;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 */
@Immutable
public abstract class ExternalPerson extends Person implements ExternalIdentity {
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Stores the presumable address of this external person.
     * The address is updated when the person is relocated or merged.
     */
    private @Nonnull NonHostIdentifier address;
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier getAddress() {
        return address;
    }
    
    @Override
    public final void setAddress(@Nonnull Mapper.Key key, @Nonnull InternalNonHostIdentifier address) {
        key.hashCode();
        this.address = address;
    }
    
}
