package net.digitalid.service.core.setup;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Server} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public class ServerSetup extends DatabaseSetup {
    
    private static @Nonnull HostIdentifier recipient;
    
    protected static @Nonnull HostIdentifier getRecipient() {
        return recipient;
    }
    
    @BeforeClass
    public static void setUpServer() {
        Server.start("test.digitalid.net");
        recipient = new HostIdentifier("test.digitalid.net");
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
