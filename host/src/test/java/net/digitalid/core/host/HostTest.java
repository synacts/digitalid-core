package net.digitalid.core.host;

import javax.annotation.Nonnull;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.identification.identifier.HostIdentifier;

import org.junit.Test;

public class HostTest extends CryptographyTestBase {
    
    @Test
    public void testHostCreation() {
        final @Nonnull Host host = HostBuilder.withIdentifier(HostIdentifier.with("test.digitalid.net")).build();
        assertEquals("test.digitalid.net", host.getIdentifier().getString());
    }
    
}
