package net.digitalid.core.server;

import javax.annotation.Nonnull;

import net.digitalid.core.identifier.HostIdentifier;

import net.digitalid.core.server.Server;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up the {@link Server} for testing.
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
