package net.digitalid.core.server;

import javax.annotation.Nonnull;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.host.Host;
import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Server} for testing.
 */
public class ServerSetup extends CryptographyTestBase {
    
    protected static @Nonnull HostIdentifier identifier;
    
    protected static @Nonnull Host host;
    
    @BeforeClass
    public static void setUpServer() {
        Server.start();
        identifier = HostIdentifier.with("test.digitalid.net");
        host = HostBuilder.withIdentifier(identifier).build();
        Server.addHost(host);
    }
    
    @AfterClass
    public static void breakDownServer() {
        Server.stop();
    }
    
    @Test
    public final void testServerSetup() {
        if (getClass().equals(ServerSetup.class)) {
            Assert.assertTrue(Server.hasHost(identifier));
        }
    }
    
}
