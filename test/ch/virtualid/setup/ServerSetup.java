package ch.virtualid.setup;

import ch.virtualid.identity.HostIdentifier;
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
    
    protected final HostIdentifier recipient = new HostIdentifier("syntacts.com");
    
    @BeforeClass
    public static void setUpServer() {
        Server.start("syntacts.com");
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
