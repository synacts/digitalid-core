package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.MobileIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models a mobile person.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class MobilePerson extends ExternalPerson implements Immutable {
    
    /**
     * Stores the semantic type {@code mobile.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("mobile.person@virtualid.ch").load(ExternalPerson.IDENTIFIER);
    
    
    /**
     * Creates a new mobile person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the address of this mobile person.
     */
    MobilePerson(long number, @Nonnull MobileIdentifier address) {
        super(number, address);
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.MOBILE_PERSON;
    }
    
}
