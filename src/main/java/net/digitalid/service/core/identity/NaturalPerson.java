package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class models a natural person.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class NaturalPerson extends InternalPerson {
    
    /**
     * Stores the semantic type {@code natural.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("natural.person@core.digitalid.net").load(InternalPerson.IDENTIFIER);
    
    
    /**
     * Creates a new natural person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    NaturalPerson(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.NATURAL_PERSON;
    }
    
}
