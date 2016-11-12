package net.digitalid.core.property.generation;

import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

@Immutable
@GenerateSubclass
@GenerateConverter
abstract class GeneratePropertyClass /* TODO: extends CoreConcept<NonHostEntity, None> */ {
    
//    @Pure
//    @Default("\"\"") // TODO
//    @GenerateSynchronizedProperty() // TODO
//    public abstract @Nonnull WritablePersistentValueProperty<GeneratePropertyClass, @Nonnull @MaxSize(50) String> password();
    
}

public class GeneratePropertyTest {
    
}
