package ch.virtualid.client;

import ch.virtualid.database.Database;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Service;
import ch.virtualid.module.client.Synchronization;
import ch.virtualid.util.ReadonlyList;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.1
 */
public final class Synchronizer extends Thread {
    
    private static final @Nonnull Queue<Action> queue; // should be synchronized
    
    /**
     * Stores the types of the modules that are currently suspended.
     */
    private static final @Nonnull Set<SemanticType> suspendedModules = new HashSet<SemanticType>();
    
    static {
        // Load the queue from the database.
        // Create a thread and enter run().
    }
    
    /**
     * Executes the given action on the client and 
     * 
     * @param action
     */
    public static void execute(@Nonnull InternalAction action) throws SQLException {
        assert action.isOnClient() : "The internal action is on the client.";
        
        action.getModule(); // TODO: Make sure the module is not suspended. Otherwise, pause until it's no longer suspended.
        
        // TODO: Include the entity, recipient and subject in the queue! + service
        Synchronization.queue(action); // Writes the action with its entity to the database through the connection of the action without commit. -> Include the name of the client in the database table.
        action.executeOnClient();
        Database.getConnection().commit();
        queue.add(action);
    }
    
    
    public static void reload(@Nonnull Service service) {
        final @Nonnull ReadonlyList<BothModule> modules = service.getModules();
        // TODO: Suspend all modules.
        // TODO: Do the magic.
        // TODO: Release all modules again.
    }
    
    
    /**
     * Updates are done in a regular interval until this boolean becomes false.
     */
    private boolean active = true;
    
    /*
    
    Required tables:
    - action: 
    - queue:
    - last:
    
    */
    
    /**
     * Asynchronous method to handle the incoming request.
     */
    @Override
    public void run() {
        while (active) {
            try {
                // Take all the actions with the same entity, recipient and subject from the queue. + service
                // Sign, pack and send them. (Signature depends on InternalAction vs. ExternalAction and getRequiredPermissions().)
                // Wait for the response.
                // Undo failed actions and remove all from the queue (both locally and in the DB).
                // 
            } catch (@Nonnull SQLException exception) {
                    System.err.println(exception);
            }
            
            try {
                sleep(100); // Alternatively, make blocking call on the queue?
            } catch (@Nonnull InterruptedException exception) {
                System.err.println(exception);
            }
        }
    }
    
    /**
     * Shuts down the synchronizer after having finished the current update.
     */
    void shutDown() {
        active = false;
    }
    
//                try {
//                    synchronizer.shutDown();
//                    synchronizer.join();
//                } catch (InterruptedException exception) {
//                    console.write("The synchronizer could not be stopped properly!");
//                }
    
}
