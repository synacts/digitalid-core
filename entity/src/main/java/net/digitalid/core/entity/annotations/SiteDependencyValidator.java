package net.digitalid.core.entity.annotations;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.validation.annotations.type.Stateless;
import net.digitalid.utility.validation.validator.ValueAnnotationValidator;

/**
 * This class declares the target types for site-dependency annotations.
 */
@Stateless
public abstract class SiteDependencyValidator implements ValueAnnotationValidator {
    
    /* -------------------------------------------------- Target Types -------------------------------------------------- */
    
    private static final @Nonnull FiniteIterable<@Nonnull Class<?>> targetTypes = FiniteIterable.of(SiteDependency.class);
    
    @Pure
    @Override
    public @Nonnull FiniteIterable<@Nonnull Class<?>> getTargetTypes() {
        return targetTypes;
    }
    
}
