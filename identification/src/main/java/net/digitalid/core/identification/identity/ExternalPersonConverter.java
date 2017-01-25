package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers an external person to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class ExternalPersonConverter extends IdentityConverter<ExternalPerson> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull ExternalPersonConverter INSTANCE = new ExternalPersonConverterSubclass(ExternalPerson.class);
    
}
