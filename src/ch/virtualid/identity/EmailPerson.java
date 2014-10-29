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
     * Stores the address of this email person.
     */
    private final @Nonnull EmailIdentifier address;
    
    /**
     * Creates a new email person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the address of this email person.
     */
    EmailPerson(long number, @Nonnull EmailIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public @Nonnull EmailIdentifier getAddress() {
        return address;
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
}
