package net.digitalid.core.property.generation;

import javax.annotation.Generated;
import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.property.nonnullable.WritableNonNullableProperty;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.auxiliary.None;

import net.digitalid.core.concept.CoreConcept;
import net.digitalid.core.concept.annotations.GenerateProperty;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.property.nonnullable.NonNullableConceptProperty;

/**
 *
 */
@GenerateSubclass
@GenerateConverter
abstract class GeneratePropertyClass extends CoreConcept<NonHostEntity, None> {
    
    /**
     * Stores a property.
     */
    @Pure
    @Default("\"\"")
    @GenerateProperty()
    public abstract @Nonnull NonNullableConceptProperty<@MaxSize(50) String, GeneratePropertyClass, NonHostEntity> password();
    
}

public class GeneratePropertyTest {
    
}
