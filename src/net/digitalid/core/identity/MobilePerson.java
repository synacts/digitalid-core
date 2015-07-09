package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.MobileIdentifier;

/**
 * This class models a mobile person.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class MobilePerson extends ExternalPerson {
    
    /**
     * Stores the semantic type {@code mobile.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("mobile.person@core.digitalid.net").load(ExternalPerson.IDENTIFIER);
    
    
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
