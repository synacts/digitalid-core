package net.digitalid.core.server;

import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.Nonnull;

import net.digitalid.core.host.Host;
import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.packet.Request;
import net.digitalid.core.testing.CoreTest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Server} for testing.
 */
public class ServerSetup extends CoreTest {
    
    protected static @Nonnull HostIdentifier identifier;
    
    protected static @Nonnull Host host;
    
    @BeforeClass
    public static void setUpServer() throws IOException {
        Server.start();
        identifier = HostIdentifier.with("test.digitalid.net");
        host = HostBuilder.withIdentifier(identifier).build();
        Server.addHost(host);
        
        Request.ADDRESS.set(identifier -> InetAddress.getLoopbackAddress());
        Request.TIMEOUT.set(900000); // 15 minutes
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
