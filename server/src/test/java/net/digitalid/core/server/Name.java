package net.digitalid.core.server;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.pack.Packable;

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public interface Name extends Packable {
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    public static final @Nonnull @Loaded SemanticType TYPE = SemanticType.map(NameConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.STRING).withCategories(Category.INTERNAL_NON_HOST_IDENTITIES).withCachingPeriod(Time.MONTH).build());
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    @Pure
    public @Nonnull String getValue();
    
}
