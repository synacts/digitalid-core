package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers a person to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class PersonConverter extends IdentityConverter<Person> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull PersonConverter INSTANCE = new PersonConverterSubclass(Person.class);
    
}
