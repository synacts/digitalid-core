package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers a non-host identity to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class ArtificialPersonConverter extends IdentityConverter<ArtificialPerson> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull ArtificialPersonConverter INSTANCE = new ArtificialPersonConverterSubclass(ArtificialPerson.class);
    
}
