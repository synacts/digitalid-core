package ch.virtualid.server;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Directory;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import java.io.File;
import java.math.BigInteger;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * System testing of the Virtual ID {@link Server}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.4
 */
public class ServerTest {
    
    /**
     * Tests the initialization of the server, the creation of hosts and the management of VIDs.
     */
    @Test
    public void testServer() throws Exception {
        // Files
        Directory.empty(Directory.CLIENTS);
        Directory.empty(Directory.HOSTS);
        
        // Database
        File file = new File(Directory.ROOT.getPath() + Directory.SEPARATOR + "Database.xdf");
        assertTrue(file.exists());
        Database.clear();
        
//    /**
//     * Clears the database.
//     */
//    public static void clear() throws SQLException {
//        Connection connection = null;
//        try {
//            connection = getConnection();
//            Statement statement = connection.createStatement();
//            statement.executeUpdate("DROP DATABASE IF EXISTS `" + database + "`");
//            statement.executeUpdate("CREATE DATABASE `" + database + "`");
//            connection.commit();
//        } catch (SQLException exception) {
//            if (connection != null) connection.rollback();
//            throw exception;
//        } finally { if (connection != null) connection.close(); }
//    }
    
        
        // Server
        Server.start(new String[] {"test.virtualid.ch"});
        Client client = new Client("Tester");
        
        // Hosting and Category
        Request.openAccount(client, "person@test.virtualid.ch", client.getName(), Category.NATURAL_PERSON);
        long vid = Mapper.getVid("person@test.virtualid.ch");
        assertTrue(Category.isNaturalPerson(vid));
        
        // Attributes
        Block block = new SelfcontainedWrapper("name@virtualid.ch", new StringWrapper("Person").toBlock()).getBlock();
        Request.setAttribute(client, vid, new SignatureWrapper(block).getBlock(), "everybody");
        assertEquals("Person", new StringWrapper(Request.getAttributeNotNullUnwrapped(vid, Mapper.getVid("name@virtualid.ch"))).getString());
        
        // Contacts
        Request.addContact(client, vid, vid, 0);
        List<Long> contacts = Request.getContacts(client, vid, 0);
        assertTrue(contacts.size() == 1 && contacts.contains(vid));
        
        // Clients (1)
        Restrictions restrictions = new Restrictions(0, true, 0l);
        AgentPermissions authorization = new AgentPermissions(SemanticType.CLIENT_GENERAL_PERMISSION, true);
        
        assertEquals(restrictions, Request.getRestrictions(client, vid));
        assertEquals(authorization, Request.getAuthorization(client, vid));
        
        // Credentials
        client.getCredential(vid, vid, new RandomizedAuthorization(authorization));
        
        Request.openAccount(client, "other@test.virtualid.ch", client.getName(), Category.NATURAL_PERSON);
        long other = Mapper.getVid("other@test.virtualid.ch");
        block = new SelfcontainedWrapper("name@virtualid.ch", new StringWrapper("Other").toBlock()).getBlock();
        Request.setAttribute(client, other, new SignatureWrapper(block).getBlock(), "person@test.virtualid.ch");
        assertEquals("Other", new StringWrapper(Request.getAttributeNotNullUnwrapped(client, vid, other, Mapper.getVid("name@virtualid.ch"))).getString());
        
        // Clients (2)
        List<Block> clients = Request.getClients(client, vid);
        assertTrue(clients.size() == 1);
        Block[] elements = new TupleWrapper(clients.get(0)).getElementsNotNull(5);
        BigInteger commitment = new IntegerWrapper(elements[0]).getValue();
        assertEquals("Tester", new StringWrapper(elements[1]).getString());
        assertEquals(restrictions, new Restrictions(elements[2]));
        assertEquals(authorization, new AgentPermissions(elements[3]));
        assertEquals(authorization, new AgentPermissions(elements[4]));
        Request.authorizeClient(client, vid, commitment, restrictions, authorization);
        Request.removeClient(client, vid, commitment);
    }
    
}
