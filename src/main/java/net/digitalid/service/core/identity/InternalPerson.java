package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class models an internal person.
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 */
@Immutable
public abstract class InternalPerson extends Person implements InternalNonHostIdentity {
    
    /**
     * Stores the semantic type {@code internal.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("internal.person@core.digitalid.net").load(Person.IDENTIFIER);
    
    
    /**
     * Stores the presumable address of this internal person.
     * The address is updated when the person is relocated or merged.
     */
    private @Nonnull InternalNonHostIdentifier address;
    
    /**
     * Creates a new internal person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this internal person.
     */
    InternalPerson(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentifier getAddress() {
        return address;
    }
    
    @Override
    public final void setAddress(@Nonnull Mapper.Key key, @Nonnull InternalNonHostIdentifier address) {
        key.hashCode();
        this.address = address;
    }
    
}
