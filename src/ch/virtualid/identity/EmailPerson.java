package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identifier.EmailIdentifier;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models an email person.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class EmailPerson extends ExternalPerson implements Immutable {
    
    /**
     * Stores the semantic type {@code email.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("email.person@virtualid.ch").load(ExternalPerson.IDENTIFIER);
    
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    EmailPerson(long number, @Nonnull EmailIdentifier address) {
        super(number, address);
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
    
    @Pure
    @Override
    public @Nonnull EmailIdentifier getExternalAddress() {
        assert address instanceof EmailIdentifier : "The address is an email identifier.";
        return (EmailIdentifier) address;
    }
    
}
