package ch.virtualid.synchronizer;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.handler.Reply;
import ch.virtualid.io.Level;
import ch.virtualid.io.Logger;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.Service;
import ch.virtualid.packet.Response;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentHashSet;
import ch.virtualid.util.ConcurrentMap;
import ch.virtualid.util.ConcurrentSet;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyCollection;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
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
    static final @Nonnull Logger LOGGER = new Logger("Synchronizer.log");
    
    /**
     * Executes the given action on the client and queues it for delivery.
     * In case the given action is not similar to itself, it is executed
     * only <em>after</em> being sent to the host. If the send method is
     * overridden by the given action, it is ignored by the synchronizer.
     * 
     * @param action the action which is to be executed and sent to the host.
     * 
     * @require action.isOnClient() : "The internal action is on a client.";
     */
    public static void execute(@Nonnull InternalAction action) throws SQLException {
        assert action.isOnClient() : "The internal action is on a client.";
        
        if (action.isSimilarTo(action)) action.executeOnClient();
        Synchronization.add(action);
        Database.commit();
    }
    
    
    /**
     * Stores the services that are currently suspended because a sender is using it.
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentSet<Service>> suspendedServices = new ConcurrentHashMap<Role, ConcurrentSet<Service>>();
    
    /**
     * Returns whether the given service is suspended for the given role.
     * 
     * @param role the role for which the service needs to be checked.
     * @param service the service which needs to checked for the role.
     * 
     * @return whether the given service is suspended for the given role.
     */
    private static boolean isSuspended(@Nonnull Role role, @Nonnull Service service) {
        final @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
        if (set != null) return set.contains(service);
        else return false;
    }
    
    /**
     * Suspends the given service for the given role.
     * 
     * @param role the role for which the service is to be suspended.
     * @param service the service which is to be suspended for the given role.
     * 
     * @return whether the given service was not suspended by another thread.
     * 
     * @require !isSuspended(role, service) : "The given service is not suspended.";
     * 
     * @ensure isSuspended(role, service) : "The given service is suspended.";
     */
    private static boolean suspend(@Nonnull Role role, @Nonnull Service service) {
        assert !isSuspended(role, service) : "The given service is not suspended.";
        
        @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
        if (set == null) set = suspendedServices.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<Service>());
        return set.add(service);
    }
    
    /**
     * Resumes the given service for the given role.
     * 
     * @param role the role for which the service is to be resumed.
     * @param service the service which is to be resumed for the given role.
     * 
     * @return whether the given service was not resumed by another thread.
     * 
     * @require isSuspended(role, service) : "The given service is suspended.";
     * 
     * @ensure !isSuspended(role, service) : "The given service is not suspended.";
     */
    @SuppressWarnings("NotifyNotInSynchronizedContext")
    private static boolean resume(@Nonnull Role role, @Nonnull Service service) {
        assert isSuspended(role, service) : "The given service is suspended.";
        
        final @Nonnull ConcurrentSet<Service> set = suspendedServices.get(role);
        final boolean result = set.remove(service);
        set.notifyAll();
        return result;
    }
    
    /**
     * Reloads the state of the given service for the given role.
     * This method blocks until the given service is no longer suspended.
     * 
     * @param role the role for which the service is to be reloaded.
     * @param service the service which is to be reloaded for the given role.
     */
    @SuppressWarnings("WaitWhileNotSynced")
    public static void reload(@Nonnull Role role, @Nonnull Service service) throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
        if (set == null) set = suspendedServices.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<Service>());
        while (isSuspended(role, service) || !suspend(role, service)) set.wait();
        
        Reply response = service.getInternalQuery(role).sendNotNull();
        final @Nonnull ReadonlyCollection<BothModule> modules = service.getBothModules();
        // TODO: Suspend all modules.
        // TODO: Do the magic.
        // TODO: Release all modules again and execute the pending actions of these modules (again).
        resume(role, service);
    }
    
    
    /**
     * The thread pool executor runs the {@link Sender senders} that send the {@link InternalAction internal actions}.
     */
    private static final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), new ThreadPoolExecutor.AbortPolicy());
    
    // TODO: Subscribe to role deletions and remove them then from the deque and map (add the pending actions to the error module).
    
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
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (@Nonnull InterruptedException exception) {
            LOGGER.log(Level.WARNING, exception);
        }
    }
    
    /**
     * Stores the current interval for exponential backoff in milliseconds. –> per role?
     */
    private static long backoff = 1000l;
    
    /**
     * Sends the pending actions.
     */
    @Override
    public void run() {
        while (active) {
            try {
                final @Nullable InternalAction reference = pendingActions.poll(5L, TimeUnit.SECONDS);
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
