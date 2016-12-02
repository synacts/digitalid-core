package net.digitalid.core.expression;


import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.selfcontained.Selfcontained;

/**
 * This class models impersonal expressions.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class ImpersonalExpression extends AbstractExpression {
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that(getExpression().isImpersonal()).orThrow("This expression has to be impersonal but was $.", getString());
        super.validate();
    }
    
    /* -------------------------------------------------- Aggregation -------------------------------------------------- */
    
    /**
     * Returns whether this impersonal expression matches the given attribute content.
     */
    @Pure
    public boolean matches(@Nonnull Selfcontained attributeContent) {
        return getExpression().matches(attributeContent);
    }
    
}
