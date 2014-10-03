package ch.virtualid.client;

import ch.virtualid.agent.RandomizedAgentPermissions;
import ch.virtualid.credential.Credential;
import ch.virtualid.database.Database;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Service;
import ch.virtualid.module.client.Synchronization;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    
    // TODO: Just copied from the old implementation!
    
    /**
     * Returns the time of the last request to the given VID.
     * 
     * @param vid the VID of interest.
     * @return the time of the last request to the given VID.
     * @require isNative(vid) : "This client is accredited at the given VID.";
     */
    public synchronized long getTimeOfLastRequest(long vid) {
        assert isNative(vid) : "This client is accredited at the given VID.";
        
        return natives.get(vid);
    }
    
    /**
     * Sets the time of the last request to the given VID.
     * 
     * @param identity the VID of the last request.
     * @param time the time of the last request.
     * @require Mapper.isVid(vid) : "The first number has to denote a VID.";
     * @require time > 0 : "The time value is positive.";
     */
    synchronized void setTimeOfLastRequest(@Nonnull Identity identity, long time) throws SQLException {
        assert time > 0 : "The time value is positive.";
        
        if (!isNative(identity)) credentials.put(identity, new HashMap<Long, Map<RandomizedAgentPermissions, Credential>>());
        
        try (@Nonnull Connection connection = Database.getConnection(); @Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("REPLACE INTO " + getName() + "_natives (vid, time) VALUES (" + identity + ", " + time + ")");
            connection.commit();
        }
        
        natives.put(identity, time);
    }
    
    private void audit(long auditTime, @Nullable List<Block> auditTrail) {
        // TODO
    }
    
}
