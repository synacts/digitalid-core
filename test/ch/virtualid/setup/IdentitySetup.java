package ch.virtualid.setup;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import java.io.IOException;
import java.sql.SQLException;
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
    
    private static @Nonnull Client client;
    
    protected static @Nonnull Client getClient() {
        return client;
    }
    
    protected static final @Nonnull InternalNonHostIdentifier subject = (InternalNonHostIdentifier) IdentifierClass.create("person@example.com");
    
    private static @Nonnull Role role;
    
    protected static @Nonnull Role getRole() {
        return role;
    }
    
    @BeforeClass
    public static void setUpIdentity() throws SQLException, IOException, PacketException, ExternalException {
        client = new Client("tester", "Test Client", new Image("/ch/virtualid/resources/Host.png"), AgentPermissions.GENERAL_WRITE);
        role = client.openAccount(subject, Category.NATURAL_PERSON);
    }
    
    @AfterClass
    public static void breakDownIdentity() {}
    
    @Test
    public final void testIdentitySetup() {
        // TODO
    }
    
}
