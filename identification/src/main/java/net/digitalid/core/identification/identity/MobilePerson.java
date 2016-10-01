package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.Category;
import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This class models a mobile person.
 */
@Mutable
@GenerateSubclass
@GenerateConverter
public abstract class MobilePerson extends ExternalPerson {
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    @Default("Category.MOBILE_PERSON")
    public abstract @Nonnull Category getCategory();
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns the identity of the given address.
     */
    @Pure
    @Recover // TODO: Split into @Recover(Representation.INTERNAL) and @Recover(Representation.EXTERNAL) or something similar.
    static @Nonnull MobilePerson with(long key, @Nonnull NonHostIdentifier address) {
        try {
            // TODO: The following cast should probably not throw an internal exception.
            return IdentifierResolver.resolve(address).castTo(MobilePerson.class);
        } catch (@Nonnull ExternalException exception) {
            // TODO: How to handle this?
            throw new RuntimeException(exception);
        }
    }
    
}
