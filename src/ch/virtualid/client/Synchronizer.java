package ch.virtualid.client;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.InternalMethod;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import ch.virtualid.module.Service;
import ch.virtualid.module.client.Synchronization;
import ch.virtualid.packet.RequestAudit;
import ch.virtualid.packet.Response;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class synchronizes {@link InternalAction internal actions}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Synchronizer extends Thread {
    
    /**
     * Stores the logger of the synchronizer.
     */
    private static final @Nonnull Logger LOGGER = new Logger("Synchronizer.log");
    
    /**
     * Stores the pending actions of the synchronizer.
     */
    private static final @Nonnull BlockingDeque<InternalAction> pendingActions = new LinkedBlockingDeque<InternalAction>();
    
    /**
     * Stores the types of the modules that are currently suspended.
     */
    private static final @Nonnull ConcurrentMap<SemanticType, Object> suspendedModules = new ConcurrentHashMap<SemanticType, Object>();
    
    /**
     * Loads the pending actions of the given client from the database.
     * 
     * @param client the client whose pending actions are to be loaded.
     */
    static void load(@Nonnull Client client) {
        // TODO: If the same client runs in several processes (on different machines), make sure the pending actions are loaded only once.
        Synchronization.load(client, pendingActions);
    }
    
    /**
     * Executes the given action on the client and queues it for delivery.
     * 
     * @param action the action which is to be executed and sent to the host.
     * 
     * @require action.isOnClient() : "The internal action is on the client.";
     */
    public static void execute(@Nonnull InternalAction action) throws SQLException {
        assert action.isOnClient() : "The internal action is on the client.";
        
        if (action.isSimilarTo(action)) action.executeOnClient();
        Synchronization.queue(action);
        pendingActions.add(action);
        Database.commit();
        
//        final @Nonnull SemanticType module = action.getModule(); // TODO: Make sure the module is not suspended. Otherwise, pause until it's no longer suspended.
    }
    
    
    public static void reload(@Nonnull Service service) {
//        final @Nonnull ReadonlyList<BothModule> modules = service.getModules();
        // TODO: Suspend all modules.
        // TODO: Do the magic.
        // TODO: Release all modules again.
    }
    
    
    public static @Nonnull RequestAudit getRequestAudit(@Nonnull Method method) {
        return method instanceof InternalMethod && method.isSimilarTo(method) ? 
        if (method instanceof InternalMethod) {
            
        }
        final @Nullable RequestAudit requestAudit = isSimilarTo(this) ? new RequestAudit(Time.MIN) : null; // TODO: Synchronizer.getAudit(service);
    }
    
    
    public static void execute(@Nonnull ReadonlyList<Block> trail) {
        
    }
    
    
    /**
     * Stores an instance of the synchronizer to be run as a thread.
     */
    private static final @Nonnull Synchronizer synchronizer = new Synchronizer();
    
    static { synchronizer.start(); }
    
    /**
     * Updates are done in a regular interval until this boolean becomes false.
     */
    private static boolean active = true;
    
    /**
     * Shuts down the synchronizer after having finished the current update.
     */
    public static void shutDown() {
        active = false;
        
        try {
            synchronizer.join();
        } catch (@Nonnull InterruptedException exception) {
            LOGGER.log(Level.WARNING, exception);
        }
    }
    
    /**
     * Stores the current interval for exponential backoff in milliseconds.
     */
    private static long backoff = 1000l;
    
    /**
     * Sends the pending actions.
     */
    @Override
    public void run() {
        while (active) {
            try {
                final @Nullable InternalAction reference = pendingActions.poll(1L, TimeUnit.SECONDS);
                if (reference != null) {
                    
                    @Nonnull FreezableList<Method> methods = new FreezableLinkedList<Method>(reference);
                    final @Nonnull Iterator<InternalAction> iterator = pendingActions.iterator();
                    while (iterator.hasNext()) {
                        final @Nonnull InternalAction action = iterator.next();
                        if (reference.isSimilarTo(action) && action.isSimilarTo(reference)) methods.add(action);
                        else break;
                    }
                    
                    try {
                        final int size = methods.size();
                        final @Nonnull Response response = Method.send(methods.freeze(), null); // TODO: Include audit.
                        for (int i = 0; i < size; i++) {
                            try {
                                response.checkReply(i);
                                if (i == 0 && !reference.isSimilarTo(reference)) reference.executeOnClient();
                            } catch (@Nonnull PacketException exception) {
                                LOGGER.log(Level.WARNING, exception);
                                ((InternalAction) methods.getNotNull(i)).reverseOnClient();
                                // TODO: Add a notification to the error module.
                            }
                        }
                        
                        Synchronization.remove(reference.getRole(), size);
                        Database.commit();
                        for (int i = 0; i < size; i++) pendingActions.remove();
                        
                        backoff = 1000l; // Reset the backoff interval.
                    } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                        Database.rollback();
                        pendingActions.addFirst(reference);
                        // TODO: Add a notification to the error module.
                        LOGGER.log(Level.WARNING, exception);
                        sleep(backoff);
                        backoff *= 2;
                    }
                }
            } catch (@Nonnull InterruptedException | SQLException exception) {
                LOGGER.log(Level.WARNING, exception);
            }
        }
    }
    
}
