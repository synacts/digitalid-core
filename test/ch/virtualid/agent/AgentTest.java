package ch.virtualid.agent;

import ch.virtualid.attribute.AttributeType;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.query.internal.StateQuery;
import ch.virtualid.handler.reply.query.StateReply;
import ch.virtualid.module.CoreService;
import ch.virtualid.setup.IdentitySetup;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Agent agent} with its {@link Action actions}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AgentTest extends IdentitySetup {
    
    private static @Nonnull Client client;
    
    private static @Nonnull Role role;
    
    @BeforeClass
    public static void accreditClientAgent() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
        agentPermissions.put(AttributeType.NAME, true);
        agentPermissions.put(AttributeType.PRENAME, true);
        agentPermissions.put(AttributeType.SURNAME, true);
        agentPermissions.freeze();
        
        client = new Client("object", "Object Client", Image.CLIENT, agentPermissions);
        role = client.accredit(getSubject(), "");
    }
    
    @After
    public void testAgentStateEquality() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull StateReply reply = new StateQuery(role).sendNotNull();
        final @Nonnull Block state = CoreService.SERVICE.getState(role, role.getAgent());
        Assert.assertEquals(state.setType(StateReply.TYPE), reply.toBlock());
    }
    
    @Test
    public void _01_testUnremoveAgent() throws SQLException, IOException, PacketException, ExternalException {
        getRole().refreshState();
        for (final @Nonnull Agent agent : getRole().getAgent().getWeakerAgents()) {
            if (agent.getNumber() == role.getAgent().getNumber()) agent.unremove();
        }
        Assert.assertTrue(role.isAccredited());
    }
    
    @Test
    public void _02_testPermissionsAdd() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
        agentPermissions.put(AttributeType.EMAIL, true);
        agentPermissions.put(AttributeType.PHONE, false);
        agentPermissions.freeze();
        
        for (final @Nonnull Agent agent : getRole().getAgent().getWeakerAgents()) {
            if (agent.getNumber() == role.getAgent().getNumber()) agent.addPermissions(agentPermissions);
        }
        
        role.refreshState();
        final @Nonnull ReadonlyAgentPermissions permissions = role.getAgent().getPermissions();
        Assert.assertTrue(permissions.canWrite(AttributeType.EMAIL));
        Assert.assertTrue(permissions.canRead(AttributeType.PHONE));
        Assert.assertFalse(permissions.canWrite(AttributeType.PHONE));
    }
    
    @Test
    public void _03_testPermissionsRemove() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
        agentPermissions.put(AttributeType.PRENAME, true);
        agentPermissions.put(AttributeType.SURNAME, true);
        agentPermissions.freeze();
        
        role.getAgent().removePermissions(agentPermissions);
        
        role.getAgent().reset(); // Not necessary but I want to test the database state.
        final @Nonnull ReadonlyAgentPermissions permissions = role.getAgent().getPermissions();
        Assert.assertFalse(permissions.canWrite(AttributeType.PRENAME));
        Assert.assertFalse(permissions.canWrite(AttributeType.SURNAME));
        
        getRole().refreshState(); // TODO: This should only be necessary temporarily.
    }
    
}
