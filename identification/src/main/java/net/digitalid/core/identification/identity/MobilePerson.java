package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.core.identification.Category;

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
    
}
