package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers a relocatable identity to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class RelocatableIdentityConverter extends IdentityConverter<RelocatableIdentity> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull RelocatableIdentityConverter INSTANCE = new RelocatableIdentityConverterSubclass(RelocatableIdentity.class);
    
}
