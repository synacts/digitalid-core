package ch.virtualid.setup;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.entity.NativeRole;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.service.CoreService;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.After;
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
    
    private static @Nonnull NativeRole role;
    
    protected static @Nonnull NativeRole getRole() {
        return role;
    }
    
    private static @Nonnull InternalNonHostIdentity subject;
    
    protected static @Nonnull InternalNonHostIdentity getSubject() {
        return subject;
    }
    
    @BeforeClass
    public static void setUpIdentity() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        client = new Client("tester", "Test Client", Image.CLIENT, AgentPermissions.GENERAL_WRITE);
        final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("person@example.com");
        role = client.openAccount(identifier, Category.NATURAL_PERSON);
        subject = identifier.getIdentity();
    }
    
    @After
    public final void testStateEquality() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        try { role.refreshState(CoreService.SERVICE); } catch (Exception e) { e.printStackTrace(); throw e; }
        final @Nonnull Agent agent = role.getAgent();
        final @Nonnull Block beforeState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
        role.reloadState(CoreService.SERVICE);
        final @Nonnull Block afterState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
        Assert.assertEquals(beforeState, afterState);
    }
    
    @Test
    public final void testIdentitySetup() throws SQLException, IOException, PacketException, ExternalException {}
    
}
