package ch.virtualid.agent;

import ch.virtualid.attribute.AttributeType;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.contact.Context;
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
        getRole().getAgent().getWeakerAgent(role.getAgent().getNumber()).unremove();
        Assert.assertTrue(role.isAccredited());
    }
    
    @Test
    public void _02_testPermissionsAdd() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
        agentPermissions.put(AttributeType.EMAIL, true);
        agentPermissions.put(AttributeType.PHONE, false);
        agentPermissions.freeze();
        
        getRole().getAgent().getWeakerAgent(role.getAgent().getNumber()).addPermissions(agentPermissions);
        
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
    
    @Test
    public void _04_testRestrictionsReplace() throws SQLException, IOException, PacketException, ExternalException {
        getRole().getAgent().getWeakerAgent(role.getAgent().getNumber()).setRestrictions(new Restrictions(true, true, true, Context.getRoot(getRole())));
        
        role.refreshState();
        final @Nonnull Restrictions restrictions = role.getAgent().getRestrictions();
        Assert.assertTrue(restrictions.isRole());
        Assert.assertTrue(restrictions.isWriting());
    }
    
    @Test
    public void _05_testCommitmentReplace() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull ClientAgent clientAgent = role.getClientAgent();
        final @Nonnull Commitment oldCommitment = clientAgent.getCommitment();
        client.rotateSecret();
        clientAgent.reset();
        final @Nonnull Commitment newCommitment = clientAgent.getCommitment();
        Assert.assertNotEquals(oldCommitment, newCommitment);
        
        getRole().refreshState(); // TODO: This should only be necessary temporarily.
    }
    
    @Test
    public void _06_testNameReplace() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull String newName = "New Name of Client";
        final @Nonnull ClientAgent clientAgent = role.getClientAgent();
        clientAgent.setName(newName);
        clientAgent.reset(); // Not necessary but I want to test the database state.
        Assert.assertEquals(newName, clientAgent.getName());
        
        getRole().refreshState(); // TODO: This should only be necessary temporarily.
    }
    
    @Test
    public void _07_testIconReplace() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull Image newIcon = Image.CONTEXT;
        final @Nonnull ClientAgent clientAgent = role.getClientAgent();
        clientAgent.setIcon(newIcon);
        clientAgent.reset(); // Not necessary but I want to test the database state.
        Assert.assertEquals(newIcon, clientAgent.getIcon());
        
        getRole().refreshState(); // TODO: This should only be necessary temporarily.
    }
    
}
