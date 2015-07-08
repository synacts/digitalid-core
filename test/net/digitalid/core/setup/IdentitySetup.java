package net.digitalid.core.setup;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.AgentPermissions;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.client.Client;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NativeRole;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identity.Category;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.NaturalPerson;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.wrappers.Block;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up an {@link Identity} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
    public static void setUpIdentity() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        print("setUpIdentity");
        try {
            client = new Client("tester", "Test Client", AgentPermissions.GENERAL_WRITE);
            final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("person@test.digitalid.net");
            role = client.openAccount(identifier, Category.NATURAL_PERSON);
            subject = identifier.getIdentity().toNaturalPerson();
            Database.commit();
        } catch (@Nonnull InterruptedException | SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @After
    @Committing
    public final void testStateEquality() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        try {
            role.waitForCompletion(CoreService.SERVICE);
            Thread.sleep(1l);
            
            System.out.println("\nAfter:");
            System.out.flush();
            
            try { role.refreshState(CoreService.SERVICE); } catch (InterruptedException | SQLException | IOException | PacketException | ExternalException e) { e.printStackTrace(); throw e; }
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
    public final void testIdentitySetup() {}
    
}
