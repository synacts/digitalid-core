package ch.virtualid.setup;

import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.server.Server;
import javax.annotation.Nonnull;
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
    
    private static @Nonnull HostIdentifier recipient;
    
    protected static @Nonnull HostIdentifier getRecipient() {
        return recipient;
    }
    
    @BeforeClass
    public static void setUpServer() {
        Server.start("example.com");
        recipient = new HostIdentifier("example.com");
    }
    
    @AfterClass
    public static void breakDownServer() {
        Server.stop();
    }
    
    @Test
    public final void testServerSetup() {
        if (getClass().equals(ServerSetup.class)) {
            Assert.assertTrue(Server.hasHost(recipient));
        }
    }
    
}
