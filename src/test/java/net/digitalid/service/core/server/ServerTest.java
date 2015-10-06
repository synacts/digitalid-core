package net.digitalid.service.core.server;

import net.digitalid.service.core.agent.FreezableAgentPermissions;
import net.digitalid.service.core.client.Client;
import net.digitalid.service.core.setup.ServerSetup;
import net.digitalid.utility.system.directory.Directory;
import org.junit.Test;

/**
 * System testing of the Digital ID {@link Server}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.1
 */
public final class ServerTest extends ServerSetup {
    
    @Test
    public void testServer() throws Exception {
        // Files
        Directory.empty(Directory.getClientsDirectory());
        Directory.empty(Directory.getHostsDirectory());
        
        // Server
        Server.start("test.digitalid.net");
        Client client = new Client("Tester", "Tester", FreezableAgentPermissions.GENERAL_WRITE);
        
//        // Hosting and Category
//        Request.openAccount(client, "person@test.digitalid.net", client.getName(), Category.NATURAL_PERSON);
//        long vid = Mapper.getVid("person@test.digitalid.net");
//        assertTrue(Category.isNaturalPerson(vid));
//        
//        // Attributes
//        Block block = new SelfcontainedWrapper("name@core.digitalid.net", new StringWrapper("Person").toBlock()).getBlock();
//        Request.setAttribute(client, vid, new SignatureWrapper(block).getBlock(), "everybody");
//        assertEquals("Person", new StringWrapper(Request.getAttributeNotNullUnwrapped(vid, Mapper.getVid("name@core.digitalid.net"))).getString());
//        
//        // Contacts
//        Request.addContact(client, vid, vid, 0);
//        List<Long> contacts = Request.getContacts(client, vid, 0);
//        assertTrue(contacts.size() == 1 && contacts.contains(vid));
//        
//        // Clients (1)
//        Restrictions restrictions = new Restrictions(0, true, 0l);
//        AgentPermissions authorization = new AgentPermissions(SemanticType.CLIENT_GENERAL_PERMISSION, true);
//        
//        assertEquals(restrictions, Request.getRestrictions(client, vid));
//        assertEquals(authorization, Request.getAuthorization(client, vid));
//        
//        // Credentials
//        client.getCredential(vid, vid, new RandomizedAuthorization(authorization));
//        
//        Request.openAccount(client, "other@test.digitalid.net", client.getName(), Category.NATURAL_PERSON);
//        long other = Mapper.getVid("other@test.digitalid.net");
//        block = new SelfcontainedWrapper("name@core.digitalid.net", new StringWrapper("Other").toBlock()).getBlock();
//        Request.setAttribute(client, other, new SignatureWrapper(block).getBlock(), "person@test.digitalid.net");
//        assertEquals("Other", new StringWrapper(Request.getAttributeNotNullUnwrapped(client, vid, other, Mapper.getVid("name@core.digitalid.net"))).getString());
//        
//        // Clients (2)
//        List<Block> clients = Request.getClients(client, vid);
//        assertTrue(clients.size() == 1);
//        Block[] elements = new TupleWrapper(clients.get(0)).getElementsNotNull(5);
//        BigInteger commitment = new IntegerWrapper(elements[0]).getValue();
//        assertEquals("Tester", new StringWrapper(elements[1]).getString());
//        assertEquals(restrictions, new Restrictions(elements[2]));
//        assertEquals(authorization, new AgentPermissions(elements[3]));
//        assertEquals(authorization, new AgentPermissions(elements[4]));
//        Request.authorizeClient(client, vid, commitment, restrictions, authorization);
//        Request.removeClient(client, vid, commitment);
    }
    
}
