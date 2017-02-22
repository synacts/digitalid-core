package net.digitalid.core.server;

import javax.annotation.Nonnull;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.server.handlers.TestQuery;
import net.digitalid.core.server.handlers.TestQueryBuilder;
import net.digitalid.core.server.handlers.TestQueryConverter;
import net.digitalid.core.server.handlers.TestReply;
import net.digitalid.core.server.handlers.TestReplyConverter;
import net.digitalid.core.testing.providers.TestPrivateKeyRetrieverBuilder;
import net.digitalid.core.testing.providers.TestPublicKeyRetrieverBuilder;

import org.junit.Test;

/**
 * System testing of the Digital ID {@link Server}.
 * <p>
 * If you run into an EOFException, the worker thread on the server crashed before sending a response.
 * You should be able to find the source of the problem in {@code server/target/test-logs/test.log}.
 */
public class ServerTest extends ServerSetup {
    
    @Test
    public void testServer() throws Exception {
        MethodIndex.add(TestQueryConverter.INSTANCE);
        
        // TODO: Remove the following three lines as soon as the cache works.
        final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
        PublicKeyRetriever.configuration.set(TestPublicKeyRetrieverBuilder.withKeyPair(keyPair).build());
        PrivateKeyRetriever.configuration.set(TestPrivateKeyRetrieverBuilder.withKeyPair(keyPair).build());
        
        final @Nonnull TestQuery query = TestQueryBuilder.withMessage("Hello from the other side!").withProvidedSubject(identifier).build();
        final @Nonnull TestReply reply = query.send(TestReplyConverter.INSTANCE);
        assertThat(reply.getMessage()).isEqualTo("Hi there!");
        
        // Files
//        Directory.empty(Directory.getClientsDirectory());
//        Directory.empty(Directory.getHostsDirectory());
        // Server
//        Server.start("test.digitalid.net");
//        Client client = new Client("Tester", "Tester", FreezableAgentPermissions.GENERAL_WRITE);
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
