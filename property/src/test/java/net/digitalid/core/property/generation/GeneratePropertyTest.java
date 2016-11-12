package net.digitalid.core.property.generation;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.None;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.concept.CoreConcept;
import net.digitalid.core.concept.annotations.GenerateSynchronizedProperty;
import net.digitalid.core.entity.NonHostEntity;

@Immutable
// TODO: @GenerateSubclass
// TODO: @GenerateConverter
abstract class GeneratePropertyClass extends CoreConcept<NonHostEntity, None> {
    
    @Pure
//    @Default("\"\"") // TODO
    @GenerateSynchronizedProperty() // TODO
    public abstract @Nonnull WritablePersistentValueProperty<GeneratePropertyClass, @Nonnull @MaxSize(50) String> password();
    
}

public class GeneratePropertyTest {
    
}
