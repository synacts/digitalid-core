package ch.virtualid.setup;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up an {@link Identity} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class IdentitySetup extends ServerSetup {
    
    private static @Nonnull Client client;
    
    protected static @Nonnull Client getClient() {
        return client;
    }
    
    private static @Nonnull InternalNonHostIdentifier subject;
    
    protected static @Nonnull InternalNonHostIdentifier getSubject() {
        return subject;
    }
    
    private static @Nonnull Role role;
    
    protected static @Nonnull Role getRole() {
        return role;
    }
    
    @BeforeClass
    public static void setUpIdentity() throws SQLException, IOException, PacketException, ExternalException {
        client = new Client("tester", "Test Client", Image.CLIENT, AgentPermissions.GENERAL_WRITE);
        subject = new InternalNonHostIdentifier("person@example.com");
        role = client.openAccount(subject, Category.NATURAL_PERSON);
    }
    
    @Test
    public final void testIdentitySetup() {
        System.out.println("The test is running!");
    }
    
}
