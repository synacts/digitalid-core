package net.digitalid.service.core.setup;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.concepts.agent.Agent;
import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.entity.NativeRole;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.Category;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.NaturalPerson;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up an {@link Identity} for testing.
 */
public class IdentitySetup extends ServerSetup {
    
    private static @Nonnull Client client;
    
    @Pure
    protected static @Nonnull Client getClient() {
        return client;
    }
    
    private static @Nonnull NativeRole role;
    
    @Pure
    protected static @Nonnull NativeRole getRole() {
        return role;
    }
    
    private static @Nonnull NaturalPerson subject;
    
    @Pure
    protected static @Nonnull NaturalPerson getSubject() {
        return subject;
    }
    
    protected static void print(@Nonnull String test) {
        System.out.println("\n----- " + test + " -----");
        System.out.println("\nDuring:");
        System.out.flush();
    }
    
    @BeforeClass
    @Committing
    public static void setUpIdentity() throws InterruptedException, DatabaseException, PacketException, ExternalException, NetworkException {
        print("setUpIdentity");
        try {
            client = new Client("tester", "Test Client", FreezableAgentPermissions.GENERAL_WRITE);
            final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("person@test.digitalid.net");
            role = client.openAccount(identifier, Category.NATURAL_PERSON);
            subject = identifier.getIdentity().castTo(NaturalPerson.class);
            Database.commit();
        } catch (@Nonnull InterruptedException | DatabaseException | PacketException | ExternalException | NetworkException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @After
    @Committing
    public final void testStateEquality() throws InterruptedException, DatabaseException, PacketException, ExternalException, NetworkException {
        try {
            role.waitForCompletion(CoreService.SERVICE);
            Thread.sleep(1l);
            
            System.out.println("\nAfter:");
            System.out.flush();
            
            try { role.refreshState(CoreService.SERVICE); } catch (InterruptedException | DatabaseException | PacketException | ExternalException | NetworkException e) { e.printStackTrace(); throw e; }
            final @Nonnull Agent agent = role.getAgent();
            
            final @Nonnull Block beforeState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
            Database.commit();
            
            try { role.reloadState(CoreService.SERVICE); } catch (InterruptedException | DatabaseException | PacketException | ExternalException | NetworkException e) { e.printStackTrace(); throw e; }
            
            final @Nonnull Block afterState = CoreService.SERVICE.getState(role, agent.getPermissions(), agent.getRestrictions(), agent);
            Database.commit();
            
            Assert.assertEquals(beforeState, afterState);
        } catch (@Nonnull InterruptedException | DatabaseException | PacketException | ExternalException | NetworkException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    public final void testIdentitySetup() {}
    
}
