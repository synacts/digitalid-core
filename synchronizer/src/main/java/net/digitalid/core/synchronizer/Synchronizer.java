package net.digitalid.core.synchronizer;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentHashSet;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.collections.concurrent.ConcurrentSet;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableHashSet;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.readonly.ReadOnlySet;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.system.thread.NamedThreadFactory;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonLocked;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;

import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.packet.Response;
import net.digitalid.service.core.storage.Service;

/**
 * This class synchronizes {@link InternalAction internal actions}.
 */
public final class Synchronizer extends Thread {
    
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
    @Locked
    @Committing
    public static void execute(@Nonnull InternalAction action) throws DatabaseException {
        Require.that(action.isOnClient()).orThrow("The internal action is on a client.");
        
        if (action.isSimilarTo(action)) {
            Log.debugging("Execute and queue on the client the action " + action + ".");
            action.executeOnClient();
        }
        SynchronizerModule.add(action);
        Database.commit();
        Thread.yield();
    }
    
    /**
     * Stores the services that are currently suspended because a sender is using it.
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentSet<Service>> suspendedServices = new ConcurrentHashMap<>();
    
    /**
     * Returns whether the given service is suspended for the given role.
     * 
     * @param role the role for which the service needs to be checked.
     * @param service the service which needs to be checked for the role.
     * 
     * @return whether the given service is suspended for the given role.
     */
    static boolean isSuspended(@Nonnull Role role, @Nonnull Service service) {
        final @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
        if (set != null) { return set.contains(service); }
        else { return false; }
    }
    
    /**
     * Suspends the given service for the given role.
     * 
     * @param role the role for which the service is to be suspended.
     * @param service the service which is to be suspended for the given role.
     * 
     * @return whether the given service was not suspended by another thread.
     * 
     * @ensure isSuspended(role, service) : "The given service is suspended.";
     */
    static boolean suspend(@Nonnull Role role, @Nonnull Service service) {
        @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
        if (set == null) { set = suspendedServices.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<Service>()); }
        final boolean result = set.add(service);
        if (result) { Log.debugging("The " + service.getName() + " of " + role.getIdentity().getAddress() + " is suspended."); }
        else { Log.debugging("The " + service.getName() + " of " + role.getIdentity().getAddress() + " was suspended by another thread."); }
        return result;
    }
    
    /**
     * Resumes the given service for the given role.
     * 
     * @param role the role for which the service is to be resumed.
     * @param service the service which is to be resumed for the given role.
     * 
     * @require isSuspended(role, service) : "The given service is suspended.";
     * 
     * @ensure !isSuspended(role, service) : "The given service is not suspended.";
     */
    static void resume(@Nonnull Role role, @Nonnull Service service) {
        Require.that(isSuspended(role, service)).orThrow("The given service is suspended.");
        
        Log.debugging("The " + service.getName() + " of " + role.getIdentity().getAddress() + " is resumed.");
        final @Nonnull ConcurrentSet<Service> set = suspendedServices.get(role);
        if (!set.remove(service)) { Log.error("The " + service.getName() + " of " + role.getIdentity().getAddress() + " was resumed by another thread."); }
        synchronized (set) { set.notifyAll(); }
    }
    
    /**
     * Reloads the state of the given module for the given role.
     * 
     * @param role the role for which the module is to be reloaded.
     * @param module the module which is to be reloaded for the given role.
     * 
     * @require isSuspended(role, module.getService()) : "The service is suspended.";
     */
    @Locked
    @Committing
    static void reloadSuspended(@Nonnull Role role, @Nonnull StateModule module) throws ExternalException {
        final @Nonnull Service service = module.getService();
        Require.that(isSuspended(role, service)).orThrow("The service is suspended.");
        
        @Nonnull Time lastTime = SynchronizerModule.getLastTime(role, service); // Read from the database in order to synchronize with other processes.
        if (module.equals(service)) { lastTime = Time.MAX; }
        Log.debugging("Send a state query for the " + module.getClass().getSimpleName() + " of " + role.getIdentity().getAddress() + " with a request audit from " + lastTime.asDate() + ".");
        final @Nonnull Response response = Method.send(new FreezableArrayList<Method>(new StateQuery(role, module)).freeze(), new RequestAudit(lastTime));
        final @Nonnull StateReply reply = response.getReplyNotNull(0);
        Log.debugging("Update the state of the " + module.getClass().getSimpleName() + " of " + role.getIdentity().getAddress() + ".");
        reply.updateState();
        
        if (module.equals(service)) { SynchronizerModule.setLastTime(role, service, response.getAuditNotNull().getThisTime()); }
        final @Nullable InternalAction lastAction = SynchronizerModule.pendingActions.peekLast();
        Database.commit();
        
        final @Nonnull ReadOnlySet<StateModule> modules = new FreezableHashSet<>(module).freeze();
        SynchronizerModule.redoPendingActions(role, module.equals(service) ? service.getBothModules() : modules, lastAction);
        if (!module.equals(service)) { response.getAuditNotNull().execute(role, service, response.getRequest().getRecipient(), ResponseAudit.emptyMethodList, modules); }
    }
    
    /**
     * Reloads the state of the given module for the given role.
     * This method blocks until the service is no longer suspended.
     * 
     * @param role the role for which the module is to be reloaded.
     * @param module the module which is to be reloaded for the given role.
     */
    @NonLocked
    @Committing
    public static void reload(@Nonnull Role role, @Nonnull StateModule module) throws InterruptedException, ExternalException {
        Require.that(!Database.isLocked()).orThrow("The database is not locked.");
        
        @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
        if (set == null) { set = suspendedServices.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<Service>()); }
        final @Nonnull Service service = module.getService();
        Log.debugging("Wait for the suspension of the " + service.getName() + " of " + role.getIdentity().getAddress() + " to reload the " + module.getClass().getSimpleName() + ".");
        synchronized (set) { while (!set.add(service)) set.wait(); }
        Log.debugging("The " + service.getName() + " of " + role.getIdentity().getAddress() + " is suspended.");
        try {
            Database.lock();
            reloadSuspended(role, module);
        } catch (@Nonnull SQLException exception) {
            Database.rollback();
            throw exception;
        } finally {
            Database.unlock();
            resume(role, service);
        }
    }
    
    /**
     * Refreshes the state of the given service for the given role.
     * This method blocks until the given service is no longer suspended.
     * 
     * @param role the role for which the service is to be refreshed.
     * @param service the service which is to be refreshed for the given role.
     */
    @NonLocked
    @Committing
    public static void refresh(@Nonnull Role role, @Nonnull Service service) throws InterruptedException, ExternalException {
        Require.that(!Database.isLocked()).orThrow("The database is not locked.");
        
        if (suspend(role, service)) {
            try {
                Database.lock();
                final @Nonnull AuditQuery auditQuery = new AuditQuery(role, service);
                final @Nonnull RequestAudit requestAudit = new RequestAudit(SynchronizerModule.getLastTime(role, service));
                Log.debugging("Send an audit query for the " + service.getName() + " of " + role.getIdentity().getAddress() + " with a " + requestAudit + ".");
                final @Nonnull Response response = Method.send(new FreezableArrayList<Method>(auditQuery).freeze(), requestAudit);
                response.getAuditNotNull().execute(role, service, auditQuery.getRecipient(), ResponseAudit.emptyMethodList, ResponseAudit.emptyModuleSet);
            } catch (@Nonnull SQLException exception) {
                Database.rollback();
                throw exception;
            } finally {
                Database.unlock();
                resume(role, service);
            }
        } else {
            @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
            if (set == null) { set = suspendedServices.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<Service>()); }
            Log.debugging("Wait for the resumption of the " + service.getName() + " of " + role.getIdentity().getAddress() + ".");
            synchronized (set) { while (set.contains(service)) set.wait(); }
        }
    }
    
    /**
     * The thread pool executor runs the {@link Sender senders} that send the pending {@link InternalAction internal actions}.
     */
    private static final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8), new NamedThreadFactory("Sender"), new ThreadPoolExecutor.AbortPolicy());
    
    /**
     * Creates a new synchronizer.
     */
    private Synchronizer() {
        super("Synchronizer");
    }
    
    /**
     * Stores an instance of the synchronizer to be run as a thread.
     */
    private static final @Nonnull Synchronizer synchronizer = new Synchronizer();
    
    static { synchronizer.start(); }
    
    /**
     * Actions are sent until this boolean becomes false.
     */
    private static volatile boolean active = true;
    
    /**
     * Shuts down the synchronizer after having sent the current actions.
     */
    public static void shutDown() {
        active = false;
        
        try {
            Log.verbose("Shutting down the synchronizer.");
            synchronizer.interrupt();
            synchronizer.join();
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(1L, TimeUnit.MINUTES);
        } catch (@Nonnull InterruptedException exception) {
            Log.warning("Could not shut down the synchronizer.", exception);
        }
    }
    
    /**
     * Stores the current interval for exponential backoff in milliseconds.
     */
    private static long backoff = 125l;
    
    /**
     * Resets the interval for exponential backoff.
     */
    public static void resetBackoffInterval() {
        backoff = 125l;
    }
    
    /**
     * Sends the pending actions.
     */
    @Override
    public void run() {
        while (active) {
            try {
                Log.verbose("Wait for the next pending action.");
                final @Nullable InternalAction action = SynchronizerModule.pendingActions.take();
                Require.that(action != null).orThrow("Just to make sure that the action is indeed not null.");
                SynchronizerModule.pendingActions.addFirst(action);
                // TODO: Rewrite such that SynchronizerModule.getMethods() does the waiting.
                final @Nonnull ReadOnlyList<Method> methods = SynchronizerModule.getMethods();
                if (!methods.isEmpty()) {
                    try {
                        Log.debugging("Create a new sender for the methods " + methods + ".");
                        threadPoolExecutor.execute(new Sender(methods));
                        resetBackoffInterval();
                    } catch (@Nonnull RejectedExecutionException exception) {
                        final @Nonnull Method reference = methods.getNonNullable(0);
                        resume(reference.getRole(), reference.getService());
                        Log.warning("Could not add a new sender.", exception);
                        Log.debugging("Sleep for " + backoff + " ms.");
                        sleep(backoff);
                        backoff *= 2;
                    }
                } else {
                    Log.debugging("Sleep for " + backoff + " ms.");
                    sleep(backoff);
                    backoff *= 2;
                }
            } catch (@Nonnull InterruptedException exception) {
                Log.debugging("Could not wait for the next pending action.", exception);
            }
        }
    }
    
}
