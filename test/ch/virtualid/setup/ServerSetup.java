package ch.virtualid.setup;

import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Server} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class ServerSetup extends DatabaseSetup {
    
    protected static final HostIdentifier recipient = new HostIdentifier("example.com");
    
    @BeforeClass
    public static void setUpServer() {
        Server.start("example.com");
    }
    
    @AfterClass
    public static void breakDownServer() {
        Server.stop();
    }
    
    @Test
    public final void testServerSetup() {
        Assert.assertTrue(Server.hasHost(recipient));
    }
    
}
