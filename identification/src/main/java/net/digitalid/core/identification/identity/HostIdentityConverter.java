package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers a host identity to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class HostIdentityConverter extends IdentityConverter<HostIdentity> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull HostIdentityConverter INSTANCE = new HostIdentityConverterSubclass(HostIdentity.class);
    
}
