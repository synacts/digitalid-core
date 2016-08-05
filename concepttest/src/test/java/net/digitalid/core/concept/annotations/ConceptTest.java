package net.digitalid.core.concept.annotations;

import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.auxiliary.None;

import net.digitalid.core.concept.CoreConcept;
import net.digitalid.core.entity.NonHostEntity;

import org.junit.Test;

/**
 *
 */
@GenerateSubclass
@GenerateConverter
abstract class GenerateInfoClass extends CoreConcept<NonHostEntity, None> {
    
}

public class ConceptTest {
    
    @Test
    public void testInfoGeneration() {
        // TODO: implement once Service.CORE is properly initialized
//        final @Nonnull String name = GenerateInfoClassSubclass.INFO.getName();
//        Assert.assertEquals("GenerateInfoClass", name);
    }
    
}
