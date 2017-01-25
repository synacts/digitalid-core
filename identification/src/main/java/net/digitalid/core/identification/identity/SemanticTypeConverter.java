package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers a semantic type to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class SemanticTypeConverter extends IdentityConverter<SemanticType> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull SemanticTypeConverter INSTANCE = new SemanticTypeConverterSubclass(SemanticType.class);
    
}
