package net.digitalid.core.server;

import net.digitalid.core.server.Server;

import net.digitalid.utility.directory.Directory;

import net.digitalid.core.agent.FreezableAgentPermissions;


import net.digitalid.core.client.Client;

import org.junit.Test;

/**
 * System testing of the Digital ID {@link Server}.
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
//        Block block = SelfcontainedWrapper.encodeNonNullable("name@core.digitalid.net", StringWrapper.encodeNonNullable("Person")).getBlock();
//        Request.setAttribute(client, vid, new SignatureWrapper(block).getBlock(), "everybody");
//        assertEquals("Person", StringWrapper.decodeNonNullable(Request.getAttributeNotNullUnwrapped(vid, Mapper.getVid("name@core.digitalid.net"))));
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
//        block = SelfcontainedWrapper.encodeNonNullable("name@core.digitalid.net", StringWrapper.encodeNonNullable("Other")).getBlock();
//        Request.setAttribute(client, other, new SignatureWrapper(block).getBlock(), "person@test.digitalid.net");
//        assertEquals("Other", StringWrapper.decodeNonNullable(Request.getAttributeNotNullUnwrapped(client, vid, other, Mapper.getVid("name@core.digitalid.net"))));
//        
//        // Clients (2)
//        List<Block> clients = Request.getClients(client, vid);
//        assertTrue(clients.size() == 1);
//        Block[] elements = TupleWrapper.decode(clients.get(0)).getElementsNotNull(5);
//        BigInteger commitment = new IntegerWrapper(elements[0]).getValue();
//        assertEquals("Tester", StringWrapper.decodeNonNullable(elements[1]));
//        assertEquals(restrictions, new Restrictions(elements[2]));
//        assertEquals(authorization, new AgentPermissions(elements[3]));
//        assertEquals(authorization, new AgentPermissions(elements[4]));
//        Request.authorizeClient(client, vid, commitment, restrictions, authorization);
//        Request.removeClient(client, vid, commitment);
    }
    
}
