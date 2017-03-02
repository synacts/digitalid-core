package net.digitalid.core.resolution;

import javax.annotation.Nonnull;

import net.digitalid.database.testing.DatabaseTest;

import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.identification.identity.SemanticType;

import org.junit.Test;

public class IdentifierResolverImplementationTest extends DatabaseTest {

    @Test
    public void testSomeMethod() {
        final @Nonnull @NonLoaded SemanticType semanticType = SemanticType.map("type@test.digitalid.net");
        final @Nonnull @NonLoaded SemanticType irgendwieAnderscht = SemanticType.map("type@test.digitalid.net");
        assertThat(semanticType.getKey()).isEqualTo(irgendwieAnderscht.getKey());
    }

}
