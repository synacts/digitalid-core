package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.NonHostIdentifier;
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
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    InternalPerson(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier getInternalAddress() {
        assert address instanceof NonHostIdentifier : "The address is a non-host identifier.";
        return (NonHostIdentifier) address;
    }
    
}
