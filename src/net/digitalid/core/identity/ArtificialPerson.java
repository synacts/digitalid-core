package net.digitalid.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identifier.InternalNonHostIdentifier;

/**
 * This class models an artificial person.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class ArtificialPerson extends InternalPerson {
    
    /**
     * Stores the semantic type {@code artificial.person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("artificial.person@core.digitalid.net").load(InternalPerson.IDENTIFIER);
    
    
    /**
     * Creates a new artificial person with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    ArtificialPerson(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.ARTIFICIAL_PERSON;
    }
    
}
