package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models a natural person.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class NaturalPerson extends InternalPerson implements Immutable {
    
    /**
     * Stores the semantic type {@code natural.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("natural.person@virtualid.ch").load(InternalPerson.IDENTIFIER);
    
    
    /**
     * Creates a new natural person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    NaturalPerson(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.NATURAL_PERSON;
    }
    
}
