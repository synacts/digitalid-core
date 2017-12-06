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

//package net.digitalid.core.server;
//
//import javax.annotation.Nonnull;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.exceptions.ExternalException;
//
//import net.digitalid.database.annotations.transaction.Committing;
//import net.digitalid.database.interfaces.Database;
//
//import net.digitalid.core.agent.Agent;
//import net.digitalid.core.agent.FreezableAgentPermissions;
//import net.digitalid.core.client.Client;
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.entity.NativeRole;
//import net.digitalid.core.exceptions.request.RequestException;
//import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
//import net.digitalid.core.identification.identity.Identity;
//import net.digitalid.core.identification.identity.NaturalPerson;
//import net.digitalid.core.service.CoreService;
//
//import net.digitalid.service.core.identity.Category;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// * Sets up an {@link Identity} for testing.
// */
//public class IdentitySetup extends ServerSetup {
//    
//    private static @Nonnull Client client;
//    
//    @Pure
//    protected static @Nonnull Client getClient() {
//        return client;
//    }
//    
//    private static @Nonnull NativeRole role;
//    
//    @Pure
//    protected static @Nonnull NativeRole getRole() {
//        return role;
//    }
//    
//    private static @Nonnull NaturalPerson subject;
//    
//    @Pure
//    protected static @Nonnull NaturalPerson getSubject() {
//        return subject;
//    }
//    
//    protected static void print(@Nonnull String test) {
//        System.out.println("\n----- " + test + " -----");
//        System.out.println("\nDuring:");
//        System.out.flush();
//    }
//    
//    @BeforeClass
//    @Committing
//    public static void setUpIdentity() throws InterruptedException, ExternalException {
//        print("setUpIdentity");
//        try {
//            client = new Client("tester", "Test Client", FreezableAgentPermissions.GENERAL_WRITE);
//            final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("person@test.digitalid.net");
//            role = client.openAccount(identifier, Category.NATURAL_PERSON);
//            subject = identifier.getIdentity().castTo(NaturalPerson.class);
//            Database.commit();
//        } catch (@Nonnull InterruptedException | DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @After
//    @Committing
//    public final void testStateEquality() throws InterruptedException, ExternalException {
//        try {
//            role.waitForCompletion(CoreService.SERVICE);
//            Thread.sleep(1l);
//            
//            System.out.println("\nAfter:");
//            System.out.flush();
//            
//            try { role.refreshState(CoreService.SERVICE); } catch (InterruptedException | DatabaseException | NetworkException | InternalException | ExternalException | RequestException e) { e.printStackTrace(); throw e; }
//            final @Nonnull Agent agent = role.getAgent();
//            
//            final @Nonnull Block beforeState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
//            Database.commit();
//            
//            try { role.reloadState(CoreService.SERVICE); } catch (InterruptedException | DatabaseException | NetworkException | InternalException | ExternalException | RequestException e) { e.printStackTrace(); throw e; }
//            
//            final @Nonnull Block afterState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
//            Database.commit();
//            
//            Assert.assertEquals(beforeState, afterState);
//        } catch (@Nonnull InterruptedException | DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    public final void testIdentitySetup() {}
//    
//}
