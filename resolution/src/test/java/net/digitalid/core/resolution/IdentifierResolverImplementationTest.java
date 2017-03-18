package net.digitalid.core.resolution;

import javax.annotation.Nonnull;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class IdentifierResolverImplementationTest extends CoreTest {

    @Test
    public void testMapping() {
        final @Nonnull @NonLoaded SemanticType mapping1 = SemanticType.map("type@test.digitalid.net");
        final @Nonnull @NonLoaded SemanticType mapping2 = SemanticType.map("type@test.digitalid.net");
        assertThat(mapping1.getKey()).isEqualTo(mapping2.getKey());
    }

}
