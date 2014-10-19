package ch.virtualid.setup;

import ch.virtualid.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Sets up the {@link Server} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class ServerSetup extends DatabaseSetup {
    
    @BeforeClass
    public static void setUpServer() {
        Server.start(new String[] {"syntacts.com"});
    }
    
    @AfterClass
    public static void breakDownServer() {
        Server.shutDown();
    }
    
}
