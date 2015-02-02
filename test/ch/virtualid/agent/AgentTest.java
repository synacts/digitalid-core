package ch.virtualid.agent;

import ch.virtualid.attribute.AttributeType;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.client.Commitment;
import ch.virtualid.contact.Context;
import ch.virtualid.database.Database;
import ch.virtualid.entity.NativeRole;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.service.CoreService;
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
    
    private static @Nonnull NativeRole role;
    
    @BeforeClass
    public static void accreditClientAgent() throws SQLException, IOException, PacketException, ExternalException {
        print("accreditClientAgent");
        try {
            final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
            agentPermissions.put(AttributeType.NAME, true);
            agentPermissions.put(AttributeType.PRENAME, true);
            agentPermissions.put(AttributeType.SURNAME, true);
            agentPermissions.freeze();
            
            client = new Client("object", "Object Client", Image.CLIENT, agentPermissions);
            role = client.accredit(getSubject(), "");
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @After
    public void testAgentStateEquality() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        try {
            role.waitForCompletion(CoreService.SERVICE);
            Thread.sleep(1l);
            
            System.out.println("\nAfter (AgentTest):");
            final @Nonnull Agent agent = role.getAgent();
            
            final @Nonnull Block beforeState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
            Database.commit();
            
            try { role.reloadState(CoreService.SERVICE); } catch (InterruptedException | SQLException | IOException | PacketException | ExternalException e) { e.printStackTrace(); throw e; }
            
            final @Nonnull Block afterState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
            Database.commit();
            
            Assert.assertEquals(beforeState, afterState);
        } catch (@Nonnull InterruptedException | SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _01_testUnremoveAgent() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        print("_01_testUnremoveAgent");
        try {
            getRole().refreshState(CoreService.SERVICE);
            getRole().getAgent().getWeakerAgent(role.getAgent().getNumber()).unremove();
            getRole().waitForCompletion(CoreService.SERVICE);
            Assert.assertTrue(role.isAccredited());
        } catch (@Nonnull InterruptedException | SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _02_testPermissionsAdd() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        print("_02_testPermissionsAdd");
        try {
            final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
            agentPermissions.put(AttributeType.EMAIL, true);
            agentPermissions.put(AttributeType.PHONE, false);
            agentPermissions.freeze();
            
            getRole().getAgent().getWeakerAgent(role.getAgent().getNumber()).addPermissions(agentPermissions);
            getRole().waitForCompletion(CoreService.SERVICE);
            
            role.refreshState(CoreService.SERVICE);
            final @Nonnull ReadonlyAgentPermissions permissions = role.getAgent().getPermissions();
            Database.commit();
            
            Assert.assertTrue(permissions.canWrite(AttributeType.EMAIL));
            Assert.assertTrue(permissions.canRead(AttributeType.PHONE));
            Assert.assertFalse(permissions.canWrite(AttributeType.PHONE));
        } catch (@Nonnull InterruptedException | SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _03_testPermissionsRemove() throws SQLException {
        print("_03_testPermissionsRemove");
        try {
            final @Nonnull ClientAgent clientAgent = role.getAgent();
            final @Nonnull AgentPermissions agentPermissions = new AgentPermissions();
            agentPermissions.put(AttributeType.PRENAME, true);
            agentPermissions.put(AttributeType.SURNAME, true);
            agentPermissions.freeze();
            
            clientAgent.removePermissions(agentPermissions);
            
            clientAgent.reset(); // Not necessary but I want to test the database state.
            final @Nonnull ReadonlyAgentPermissions permissions = clientAgent.getPermissions();
            Database.commit();
            
            Assert.assertFalse(permissions.canWrite(AttributeType.PRENAME));
            Assert.assertFalse(permissions.canWrite(AttributeType.SURNAME));
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _04_testRestrictionsReplace() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        print("_04_testRestrictionsReplace");
        try {
            getRole().getAgent().getWeakerAgent(role.getAgent().getNumber()).setRestrictions(new Restrictions(true, true, true, Context.getRoot(getRole())));
            getRole().waitForCompletion(CoreService.SERVICE);
            
            role.refreshState(CoreService.SERVICE);
            final @Nonnull Restrictions restrictions = role.getAgent().getRestrictions();
            Database.commit();
            
            Assert.assertTrue(restrictions.isRole());
            Assert.assertTrue(restrictions.isWriting());
        } catch (@Nonnull InterruptedException | SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _05_testCommitmentReplace() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        print("_05_testCommitmentReplace");
        try {
            final @Nonnull ClientAgent clientAgent = role.getAgent();
            final @Nonnull Commitment oldCommitment = clientAgent.getCommitment();
            
            client.rotateSecret();
            
            clientAgent.reset();
            final @Nonnull Commitment newCommitment = clientAgent.getCommitment();
            Database.commit();
            
            Assert.assertNotEquals(oldCommitment, newCommitment);
        } catch (@Nonnull InterruptedException | SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _06_testNameReplace() throws SQLException {
        print("_06_testNameReplace");
        try {
            final @Nonnull String newName = "New Name of Client";
            final @Nonnull ClientAgent clientAgent = role.getAgent();
            clientAgent.setName(newName);
            clientAgent.reset(); // Not necessary but I want to test the database state.
            Assert.assertEquals(newName, clientAgent.getName());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _07_testIconReplace() throws SQLException {
        print("_07_testIconReplace");
        try {
            final @Nonnull Image newIcon = Image.CONTEXT;
            final @Nonnull ClientAgent clientAgent = role.getAgent();
            clientAgent.setIcon(newIcon);
            clientAgent.reset(); // Not necessary but I want to test the database state.
            Assert.assertEquals(newIcon, clientAgent.getIcon());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public void _08_testWeakerAgents() throws SQLException {
        print("_08_testWeakerAgents");
        try {
            Assert.assertEquals(1, role.getAgent().getWeakerAgents().size());
            Assert.assertEquals(2, getRole().getAgent().getWeakerAgents().size());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
