package ch.virtualid.setup;

import ch.virtualid.client.Client;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Person;
import javax.annotation.Nonnull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up an {@link Identity} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class IdentitySetup extends ServerSetup {
    
    protected @Nonnull Client client;
    
    protected @Nonnull Person person;
    
    @BeforeClass
    public static void setUpIdentity() {
        // TODO: Create a new account at syntacts.com
    }
    
    @AfterClass
    public static void breakDownIdentity() {}
    
    @Test
    public final void testIdentitySetup() {
        // TODO
    }
    
}
