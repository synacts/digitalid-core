package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.identification.Category;
import net.digitalid.core.identification.identifier.NonHostIdentifier;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 */
@Mutable
@GenerateConverter
public abstract class ExternalPerson extends Person {
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Sets the category of this external person.
     */
    @Impure
    abstract void setCategory(@Nonnull @Invariant(condition = "category.isInternalPerson()", message = "The category has to denote an internal person.") Category category);
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns the identity of the given address.
     */
    @Pure
    @Recover // TODO: Split into @Recover(Representation.INTERNAL) and @Recover(Representation.EXTERNAL) or something similar.
    static @Nonnull ExternalPerson with(long key, @Nonnull NonHostIdentifier address) {
        try {
            // TODO: The following cast should probably not throw an internal exception.
            return IdentifierResolver.resolve(address).castTo(ExternalPerson.class);
        } catch (@Nonnull ExternalException exception) {
            // TODO: How to handle this?
            throw new RuntimeException(exception);
        }
    }
    
}
