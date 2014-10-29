package ch.virtualid.identity;

import ch.virtualid.identifier.ExternalIdentifier;
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
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    ExternalPerson(long number, @Nonnull ExternalIdentifier address) {
        super(number, address);
    }
    
}
