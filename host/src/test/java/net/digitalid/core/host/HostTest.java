package net.digitalid.core.host;

import javax.annotation.Nonnull;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.testing.CoreTest;

import org.junit.Test;

public class HostTest extends CoreTest {
    
    @Test
    public void testHostCreation() {
        final @Nonnull Host host = HostBuilder.withIdentifier(HostIdentifier.with("test.digitalid.net")).build();
        assertThat(host.getIdentifier().getString()).isEqualTo("test.digitalid.net");
    }
    
}
