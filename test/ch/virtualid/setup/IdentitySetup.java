package ch.virtualid.setup;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.annotations.Committing;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Image;
import ch.virtualid.client.Client;
import ch.virtualid.database.Database;
import ch.virtualid.entity.NativeRole;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.NaturalPerson;
import ch.virtualid.service.CoreService;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sets up an {@link Identity} for testing.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
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
            client = new Client("tester", "Test Client", Image.CLIENT, AgentPermissions.GENERAL_WRITE);
            final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("person@example.com");
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
