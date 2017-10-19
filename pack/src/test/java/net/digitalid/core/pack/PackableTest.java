package net.digitalid.core.pack;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.testing.UtilityTest;
import net.digitalid.utility.validation.annotations.type.Immutable;

import org.junit.Test;

@Immutable
@GenerateSubclass
@GenerateConverter
abstract class PackableClass extends RootClass implements Packable {
    
    @Pure
    public abstract @Nonnull String getMessage();
    
}

public class PackableTest extends UtilityTest {
    
    @Test
    public void testSomeMethod() {}
    
}
