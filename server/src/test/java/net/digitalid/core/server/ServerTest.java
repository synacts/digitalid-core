/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.server;

import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.Log;

import net.digitalid.database.interfaces.Database;

import net.digitalid.core.account.OpenAccount;
import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.asymmetrickey.PrivateKeyRetriever;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.cache.CacheQueryBuilder;
import net.digitalid.core.client.Client;
import net.digitalid.core.client.ClientBuilder;
import net.digitalid.core.client.role.NativeRole;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.expression.PassiveExpressionBuilder;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.host.Host;
import net.digitalid.core.host.HostBuilder;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.packet.Request;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.server.handlers.TestQuery;
import net.digitalid.core.server.handlers.TestQueryBuilder;
import net.digitalid.core.server.handlers.TestQueryConverter;
import net.digitalid.core.server.handlers.TestReply;
import net.digitalid.core.server.handlers.TestReplyConverter;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.SignatureBuilder;
import net.digitalid.core.signature.attribute.UncertifiedAttributeValue;
import net.digitalid.core.testing.CoreTest;
import net.digitalid.core.testing.providers.TestPrivateKeyRetrieverBuilder;
import net.digitalid.core.testing.providers.TestPublicKeyRetrieverBuilder;
import net.digitalid.core.unit.CoreUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * System testing of the Digital ID {@link Server}.
 * <p>
 * If you run into an EOFException, the worker thread on the server crashed before sending a response.
 * You should be able to find the source of the problem in {@code core/all/target/test-logs/test.log}.
 */
public class ServerTest extends CoreTest {
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    /**
     * Initializes the request parameters.
     */
    @PureWithSideEffects
    @Initialize(target = Request.class)
    public static void initializeRequest() {
        Request.ADDRESS.set(identifier -> InetAddress.getLoopbackAddress());
        Request.TIMEOUT.set(900000); // 15 minutes
    }
    
    /**
     * Initializes the method index.
     */
    @PureWithSideEffects
    @Initialize(target = MethodIndex.class)
    public static void initializeMethodIndex() {
        MethodIndex.add(TestQueryConverter.INSTANCE);
    }
    
    /* -------------------------------------------------- Setup -------------------------------------------------- */
    
    protected static @Nonnull HostIdentifier hostIdentifier;
    
    protected static @Nonnull Host host;
    
    @BeforeClass
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void startServer() throws ExternalException, IOException {
        // TODO: Remove the following three lines as soon as the cache works.
        final @Nonnull KeyPair keyPair = KeyPair.withRandomValues();
        PublicKeyRetriever.configuration.set(TestPublicKeyRetrieverBuilder.withKeyPair(keyPair).build());
        PrivateKeyRetriever.configuration.set(TestPrivateKeyRetrieverBuilder.withKeyPair(keyPair).build());
        
        System.out.println();
        System.out.println("Tables created on each CoreUnit:");
        CoreUnit.MODULE.accept(table -> System.out.println(table.getFullNameWithUnderlines()));
        System.out.println();
        
        Server.start();
        hostIdentifier = HostIdentifier.with("test.digitalid.net");
        host = HostBuilder.withIdentifier(hostIdentifier).build();
    }
    
    @AfterClass
    public static void stopServer() {
        Server.stop();
    }
    
    /* -------------------------------------------------- Tests -------------------------------------------------- */
    
    @Test
    public void testServer() throws ExternalException {
        Log.information("Started the server test.");
        final @Nonnull TestQuery query = TestQueryBuilder.withMessage("Hello from the other side!").withProvidedSubject(hostIdentifier).build();
        final @Nonnull TestReply reply = query.send(TestReplyConverter.INSTANCE);
        assertThat(reply.getMessage()).isEqualTo("Hi there!");
    }
    
    @Test
    public void testIdentifierResolution() throws ExternalException {
        Log.information("Started the identifier resolution test.");
        final @Nonnull Identifier identifier = Identifier.with("person@test.digitalid.net");
        assertThatThrownBy(identifier::resolve).isInstanceOf(RequestException.class);
    }
    
    @Test
    public void testIdentityCreation() throws ExternalException {
        Log.information("Started the identity creation test.");
        
        final @Nonnull Client client = ClientBuilder.withIdentifier("test.client.digitalid.net").withDisplayName("Test Client").withPreferredPermissions(ReadOnlyAgentPermissions.GENERAL_WRITE).build();
        final @Nonnull InternalNonHostIdentifier identifier = InternalNonHostIdentifier.with("person@test.digitalid.net");
        final @Nonnull NativeRole role = OpenAccount.of(Category.NATURAL_PERSON, identifier, client);
        
        final @Nonnull Attribute nameAttribute = Attribute.of(role, Name.TYPE);
        Database.commit(); // This is necessary because otherwise the type is mapped a second time.
        
        final @Nonnull Name name = NameBuilder.withValue("Test Person").build();
        final @Nonnull Signature<Pack> signature = SignatureBuilder.withObjectConverter(PackConverter.INSTANCE).withObject(name.pack()).withSubject(identifier).build();
        
        nameAttribute.value().set(UncertifiedAttributeValue.with(signature));
        nameAttribute.visibility().set(PassiveExpressionBuilder.withEntity(role).withString("everybody").build());
        
        final @Nonnull Name cachedName = CacheQueryBuilder.withConverter(NameConverter.INSTANCE).withRequestee(role.getIdentity()).withType(Name.TYPE).build().execute();
        assertThat(cachedName).isEqualTo(name);
    }
    
    /* -------------------------------------------------- Old -------------------------------------------------- */
    
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
