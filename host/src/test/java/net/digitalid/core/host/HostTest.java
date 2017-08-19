package net.digitalid.core.host;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.testing.CoreTest;

public class HostTest extends CoreTest {
    
//    @org.junit.Test
    @Pure
    @TODO(task = "This test does no longer work because the initializer tries to create the agent table but the referenced identity entry table is only declared in the resolution artifact.", date = "2017-08-19", author = Author.KASPAR_ETTER)
    public void testHostCreation() throws ExternalException {
        final @Nonnull Host host = HostBuilder.withIdentifier(HostIdentifier.with("test.digitalid.net")).build();
        assertThat(host.getIdentifier().getString()).isEqualTo("test.digitalid.net");
    }
    
}
