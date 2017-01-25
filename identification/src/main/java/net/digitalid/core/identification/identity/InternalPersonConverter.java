package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers an internal person to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class InternalPersonConverter extends IdentityConverter<InternalPerson> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull InternalPersonConverter INSTANCE = new InternalPersonConverterSubclass(InternalPerson.class);
    
}
