package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.Category;

/**
 * This class models an email person.
 */
@Mutable
@GenerateSubclass
// TODO: @GenerateConverter
public abstract class EmailPerson extends ExternalPerson {
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    @Default("Category.EMAIL_PERSON")
    public abstract @Nonnull Category getCategory();
    
}
