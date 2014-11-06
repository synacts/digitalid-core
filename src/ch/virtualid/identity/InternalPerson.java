package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models an internal person.
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class InternalPerson extends Person implements InternalNonHostIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code internal.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("internal.person@virtualid.ch").load(Person.IDENTIFIER);
    
    
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
