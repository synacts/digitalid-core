package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.identification.Category;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 */
@Mutable
public abstract class ExternalPerson extends Person {
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    /**
     * Sets the category of this external person.
     */
    @Impure
    abstract void setCategory(@Nonnull @Invariant(condition = "category.isInternalPerson()", message = "The category has to denote an internal person.") Category category);
    
}
