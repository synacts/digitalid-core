package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.ExternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalPerson extends Person implements ExternalIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code external.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("external.person@virtualid.ch").load(Person.IDENTIFIER);
    
    
    /**
     * Stores the presumable address of this external person.
     * The address is updated when the person is relocated or merged.
     */
    private @Nonnull NonHostIdentifier address;
    
    /**
     * Creates a new external person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this external person.
     */
    ExternalPerson(long number, @Nonnull ExternalIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier getAddress() {
        return address;
    }
    
    @Override
    final void setAddress(@Nonnull InternalNonHostIdentifier address) {
        this.address = address;
    }
    
}
