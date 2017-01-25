package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers a natural person to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class NaturalPersonConverter extends IdentityConverter<NaturalPerson> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull NaturalPersonConverter INSTANCE = new NaturalPersonConverterSubclass(NaturalPerson.class);
    
}
