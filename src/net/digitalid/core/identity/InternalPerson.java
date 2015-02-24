package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.interfaces.Immutable;

/**
 * This class models an internal person.
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class InternalPerson extends Person implements InternalNonHostIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code internal.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("internal.person@core.digitalid.net").load(Person.IDENTIFIER);
    
    
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
    final void setAddress(@Nonnull InternalNonHostIdentifier address) {
        this.address = address;
    }
    
}
