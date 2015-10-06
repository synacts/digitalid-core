package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.identifier.EmailIdentifier;

/**
 * This class models an email person.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class EmailPerson extends ExternalPerson {
    
    /**
     * Stores the semantic type {@code email.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("email.person@core.digitalid.net").load(ExternalPerson.IDENTIFIER);
    
    
    /**
     * Creates a new email person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the address of this email person.
     */
    EmailPerson(long number, @Nonnull EmailIdentifier address) {
        super(number, address);
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
}
