package net.digitalid.service.core.identity;

import net.digitalid.service.core.identity.resolution.Category;
import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class models an artificial person.
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
