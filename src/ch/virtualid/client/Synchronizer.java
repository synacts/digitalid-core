package ch.virtualid.client;

import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.module.client.Synchronization;
import java.sql.SQLException;
import java.util.Queue;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Synchronizer extends Thread {
    
    private static final Queue<Action> queue; // should be synchronized
    
    static {
        // Load the queue from the database.
        // Create a thread and enter run().
    }
    
    // TODO: Also allow queries here? -> Rather no, the replies of queries are needed immediately (and are thus blocking).
    public static void execute(@Nonnull Action action) throws SQLException {
        assert action.isOnClient() : "The action is on the client-side.";
        
        // TODO: Include the entity, recipient and subject in the queue! + service
        Synchronization.queue(action); // Writes the action with its entity to the database through the connection of the action without commit. -> Include the name of the client in the database table.
        action.executeOnClient(); // TODO: Only in case of internal requests? -> actions?!
        action.commit();
        queue.add(action);
    }
    
    
    /**
     * Updates are done in a regular interval until this boolean becomes false.
     */
    private boolean active = true;
    
    public Synchronizer() {
        
    }
    
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
