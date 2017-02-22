package net.digitalid.core.testing.providers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.math.relative.GreaterThanOrEqualTo;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributes;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.identification.identity.TypeLoader;

/**
 * This class implements the {@link TypeLoader} for unit tests.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class TestTypeLoader implements TypeLoader {
    
    @Pure
    @Override
    @NonCommitting
    public @GreaterThanOrEqualTo(-1) byte load(@Nonnull SyntacticType syntacticType) throws ExternalException {
        return 0;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull SemanticTypeAttributes load(@Nonnull SemanticType semanticType) throws ExternalException {
        return SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build();
    }
    
}
