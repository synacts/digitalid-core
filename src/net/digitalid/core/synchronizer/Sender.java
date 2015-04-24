package net.digitalid.core.synchronizer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Frozen;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEmpty;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Role;
import net.digitalid.core.error.ErrorModule;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.InternalAction;
import net.digitalid.core.handler.Method;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;
import net.digitalid.core.packet.ClientRequest;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.Service;

/**
 * A sender sends {@link InternalAction internal actions} asynchronously.
 * 
 * @see Synchronizer
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class Sender extends Thread {
    
    /**
     * Stores the methods which are sent by this sender.
     * 
     * @invariant Method.areSimilar(methods) : "The methods are similar to each other.";
     * @invariant for (Method method : methods) method instanceof InternalAction : "The methods are internal actions.";
     */
    private final @Nonnull @Frozen @NonEmpty @NonNullableElements ReadonlyList<Method> methods;
    
    /**
     * Creates a new sender with the given methods.
     * 
     * @param methods the methods which are to be sent.
     * 
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require for (Method method : methods) method instanceof InternalAction : "The methods are internal actions.";
     */
    Sender(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadonlyList<Method> methods) {
        assert Method.areSimilar(methods) : "The methods are similar to each other.";
        for (final @Nonnull Method method : methods) assert method instanceof InternalAction : "The methods are internal actions.";
        
        this.methods = methods;
    }
    
    /**
     * Sends the methods of this sender.
     */
    @Override
    @Committing
    public void run() {
        final @Nonnull InternalAction reference = (InternalAction) methods.getNotNull(0);
        final @Nonnull Role role = reference.getRole();
        final @Nonnull Service service = reference.getService();
        
        try {
            final @Nonnull RequestAudit requestAudit;
            try {
                Database.lock();
                requestAudit = new RequestAudit(SynchronizerModule.getLastTime(role, service));
                if (Database.isSingleAccess()) Database.commit();
            } finally {
                Database.unlock();
            }
            
            long backoff = 1000l;
            while (true) {
                try {
                    Database.lock();
                    final @Nonnull Response response;
                    try {
                        Logger.log(Level.DEBUGGING, "Sender", "Send the methods " + methods + " with a " + requestAudit + ".");
                        response = Method.send(methods, requestAudit);
                        Database.commit();
                    } catch (@Nonnull SQLException | PacketException | ExternalException exception) {
                        Logger.log(Level.WARNING, "Sender", "Could not send the methods " + methods + ".", exception);
                        Database.rollback();
                        for (final @Nonnull Method method : methods) ErrorModule.add("Could not send", (InternalAction) method);
                        Database.commit();
                        return;
                    }
                    
                    if (reference.isSimilarTo(reference)) {
                        for (int i = methods.size() - 1; i >= 0; i--) {
                            final @Nonnull InternalAction action = (InternalAction) methods.getNotNull(i);
                            
                            try {
                                response.checkReply(i);
                                
                                try {
                                    Logger.log(Level.DEBUGGING, "Sender", "Execute on success the action " + action + ".");
                                    action.executeOnSuccess();
                                    Database.commit();
                                } catch (@Nonnull SQLException exception) {
                                    Logger.log(Level.WARNING, "Sender", "Could not execute on success the action " + action + ".", exception);
                                    Database.rollback();
                                    ErrorModule.add("Could not execute on success", action);
                                    Database.commit();
                                }
                            } catch (@Nonnull PacketException exception) {
                                Logger.log(Level.WARNING, "Sender", "Could not execute on the host the action " + action + ".", exception);
                                ErrorModule.add("Could not execute on the host", action);
                                Database.commit();
                                
                                try {
                                    Logger.log(Level.DEBUGGING, "Sender", "Reverse on the client the action " + action + ".");
                                    action.reverseOnClient();
                                    Database.commit();
                                } catch (@Nonnull SQLException exc) {
                                    Logger.log(Level.WARNING, "Sender", "Could not reverse on the client before having reversed the interfering actions (" + action + ").", exc);
                                    Database.rollback();
                                    
                                    try {
                                        final @Nonnull ReadonlyList<InternalAction> reversedActions = SynchronizerModule.reverseInterferingActions(action);
                                        Logger.log(Level.DEBUGGING, "Sender", "Reverse on the client after having reversed the interfering actions (" + action + ").");
                                        action.reverseOnClient();
                                        Database.commit();
                                        SynchronizerModule.redoReversedActions(reversedActions);
                                    } catch (@Nonnull SQLException e) {
                                        Logger.log(Level.ERROR, "Sender", "Could not reverse on the client after having reversed the interfering actions (" + action + ").", e);
                                        Database.rollback();
                                        Synchronizer.reloadSuspended(role, service);
                                        return;
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            response.checkReply(0);
                            Logger.log(Level.DEBUGGING, "Sender", "Execute on the client the action " + reference + ".");
                            reference.executeOnClient();
                            Database.commit();
                        } catch (@Nonnull SQLException | PacketException exception) {
                            Logger.log(Level.WARNING, "Sender", "Could not execute on the client the action " + reference + ".", exception);
                            Database.rollback();
                            ErrorModule.add("Could not execute on the client", reference);
                            Database.commit();
                        }
                    }
                    
                    response.getAuditNotNull().execute(role, service, reference.getRecipient(), methods, ResponseAudit.emptyModuleSet);
                    
                    return;
                } catch (@Nonnull IOException exception) {
                    Logger.log(Level.WARNING, "Sender", "Could not send the methods " + methods + ".", exception);
                    Logger.log(Level.DEBUGGING, "Sender", "Going to sleep for " + backoff + " ms.");
                    Database.unlock();
                    try { sleep(backoff); }
                    finally { Database.lock(); }
                    backoff *= 2;
                } finally {
                    Database.unlock();
                }
            }
        } catch (@Nonnull InterruptedException | SQLException | PacketException | ExternalException exception) {
            Logger.log(Level.WARNING, "Sender", "Could not commit the transaction or reload the state.", exception);
            Database.rollback();
        } finally {
            try {
                Database.lock();
                Logger.log(Level.DEBUGGING, "Sender", "Remove the methods " + methods + ".");
                SynchronizerModule.remove(methods);
                Database.commit();
            } catch (@Nonnull SQLException exception) {
                Logger.log(Level.WARNING, "Sender", "Could not remove the methods " + methods + ".", exception);
                Database.rollback();
            } finally {
                Database.unlock();
            }
            
            Synchronizer.resume(role, service);
        }
    }
    
    
    /**
     * Executes the given action asynchronously without suspending or resuming its service.
     * 
     * @param action the action which is to be executed asynchronously.
     * @param audit the audit that was requested in the failed request.
     * 
     * @return the audit which is to be used for resending the request.
     * 
     * @see ClientRequest
     */
    @NonCommitting
    public static @Nullable RequestAudit runAsynchronously(final @Nonnull InternalAction action, final @Nullable RequestAudit audit) throws SQLException, IOException, PacketException, ExternalException {
        // TODO: This will almost certainly not work with the locking mechanism of SQLite. The problem could propably be solved with savepoints and partial rollbacks, however.
        
        Logger.log(Level.ERROR, "Sender", "The sender should not yet be run asynchronously.");
        
        new Thread("Asynchronous-Sender") {
            @Override
            @Committing
            public void run() {
                try {
                    Database.lock();
                    action.executeOnClient(); // The action is executed as soon as the database entries are no longer locked.
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Logger.log(Level.ERROR, "Sender", "Could not send the action asynchronously.", exception);
                    Database.rollback();
                } finally {
                    Database.unlock();
                }
            }
        }.start();
        
        final @Nonnull FutureTask<RequestAudit> task;
        task = new FutureTask<>(new Callable<RequestAudit>() {
            @Override
            @Committing
            public @Nullable RequestAudit call() throws Exception {
                try {
                    Database.lock();
                    final @Nonnull RequestAudit requestAudit = audit != null ? audit : new RequestAudit(SynchronizerModule.getLastTime(action.getRole(), action.getService()));
                    final @Nonnull ReadonlyList<Method> methods = new FreezableArrayList<Method>(action).freeze();
                    final @Nonnull Response response = Method.send(methods, requestAudit);
                    response.checkReply(0);
                    final @Nonnull ResponseAudit responseAudit = response.getAuditNotNull();
                    responseAudit.execute(action.getRole(), action.getService(), action.getRecipient(), methods, ResponseAudit.emptyModuleSet);
                    return audit != null ? new RequestAudit(responseAudit.getThisTime()) : null;
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    Database.rollback();
                    throw exception;
                } finally {
                    Database.unlock();
                }
            }
        });
        task.run();
        
        try {
            return task.get();
        } catch (@Nonnull InterruptedException | ExecutionException exception) {
            Logger.log(Level.ERROR, "Sender", "Could not execute the action asynchronously.", exception);
            throw new PacketException(PacketError.INTERNAL, "The action could not be executed asynchronously.", exception);
        }
    }
    
}
