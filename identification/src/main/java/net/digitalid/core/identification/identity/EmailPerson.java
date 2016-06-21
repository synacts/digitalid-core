package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.identification.Category;

/**
 * This interface models an email person.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public interface EmailPerson extends ExternalPerson {
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull Category getCategory() {
        return Category.EMAIL_PERSON;
    }
    
}
