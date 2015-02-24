package net.digitalid.core.setup;

import javax.annotation.Nonnull;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Server} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
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
