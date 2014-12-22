package ch.virtualid.setup;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.query.internal.StateQuery;
import ch.virtualid.handler.reply.query.StateReply;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.Assert;
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
    public final void testIdentitySetup() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull StateReply reply = new StateQuery(role).sendNotNull();
        final @Nonnull Block state = CoreService.SERVICE.getState(role, role.getAgent());
        System.out.println("Client: " + state);
        System.out.println("Host:   " + reply.toBlock());
        Assert.assertEquals(state, reply.toBlock());
    }
    
}
