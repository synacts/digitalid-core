package ch.virtualid.synchronizer;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Committing;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.error.ErrorModule;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.io.Level;
import ch.virtualid.packet.ClientRequest;
import ch.virtualid.packet.Response;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A sender sends {@link InternalAction internal actions} asynchronously.
 * 
 * @see Synchronizer
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Sender extends Thread {
    
    /**
     * Stores the methods which are sent by this sender.
     * 
     * @invariant methods.isFrozen() : "The list of methods is frozen.";
     * @invariant methods.isNotEmpty() : "The list of methods is not empty.";
     * @invariant methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @invariant Method.areSimilar(methods) : "The methods are similar to each other.";
     * @invariant for (Method method : methods) method instanceof InternalAction : "The methods are internal actions.";
     */
    private final @Nonnull ReadonlyList<Method> methods;
    
    /**
     * Creates a new sender with the given methods.
     * 
     * @param methods the methods which are to be sent.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require Method.areSimilar(methods) : "The methods are similar to each other.";
     * @require for (Method method : methods) method instanceof InternalAction : "The methods are internal actions.";
     */
    Sender(@Nonnull ReadonlyList<Method> methods) {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        assert methods.doesNotContainNull() : "The list of methods does not contain null.";
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
            final @Nonnull RequestAudit requestAudit = new RequestAudit(SynchronizerModule.getLastTime(role, service));
            
            long backoff = 1000l;
            while (backoff > 0l) {
                try {
                    final @Nonnull Response response;
                    try {
                        response = Method.send(methods, requestAudit);
                        Database.commit();
                    } catch (@Nonnull SQLException | PacketException | ExternalException exception) {
                        Synchronizer.LOGGER.log(Level.WARNING, "Could not send the methods", exception);
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
                                    action.executeOnSuccess();
                                    Database.commit();
                                } catch (@Nonnull SQLException exception) {
                                    Synchronizer.LOGGER.log(Level.WARNING, "Could not execute on success", exception);
                                    Database.rollback();
                                    
                                    ErrorModule.add("Could not execute on success", action);
                                    Database.commit();
                                }
                            } catch (@Nonnull PacketException exception) {
                                Synchronizer.LOGGER.log(Level.WARNING, "Could not execute on the host", exception);
                                ErrorModule.add("Could not execute on the host", action);
                                Database.commit();
                                
                                try {
                                    action.reverseOnClient();
                                    Database.commit();
                                } catch (@Nonnull SQLException exc) {
                                    Synchronizer.LOGGER.log(Level.WARNING, "Could not reverse on the client before having reversed the interfering actions", exc);
                                    Database.rollback();
                                    
                                    try {
                                        final @Nonnull ReadonlyList<InternalAction> reversedActions = SynchronizerModule.reverseInterferingActions(action);
                                        action.reverseOnClient();
                                        Database.commit();
                                        SynchronizerModule.redoReversedActions(reversedActions);
                                    } catch (@Nonnull SQLException e) {
                                        Synchronizer.LOGGER.log(Level.ERROR, "Could not reverse on the client after having reversed the interfering actions", e);
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
                            reference.executeOnClient();
                            Database.commit();
                        } catch (@Nonnull SQLException | PacketException exception) {
                            Synchronizer.LOGGER.log(Level.WARNING, "Could not execute on the client", exception);
                            Database.rollback();
                            
                            ErrorModule.add("Could not execute on the client", reference);
                            Database.commit();
                        }
                    }
                    
                    response.getAuditNotNull().execute(role, service, reference.getRecipient(), methods, ResponseAudit.emptyModuleSet);
                    
                    backoff = 0l;
                } catch (@Nonnull IOException exception) {
                    System.out.println("IOException in Sender: " + exception); // TODO: Remove eventually.
                    sleep(backoff);
                    backoff *= 2;
                }
            }
        } catch (@Nonnull InterruptedException | SQLException | PacketException | ExternalException exception) {
            Synchronizer.LOGGER.log(Level.WARNING, "Could not commit or reload", exception);
            try {
                Database.rollback();
            } catch (@Nonnull SQLException exc) {
                Synchronizer.LOGGER.log(Level.WARNING, "Could not rollback", exc);
            }
        } finally {
            try {
                SynchronizerModule.remove(methods);
                Database.commit();
            } catch (@Nonnull SQLException exception) {
                Synchronizer.LOGGER.log(Level.WARNING, "Could not remove the methods", exception);
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
        new Thread() {
            @Override
            @Committing
            public void run() {
                try {
                    action.executeOnClient(); // The action is executed as soon as the database entries are no longer locked.
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Synchronizer.LOGGER.log(Level.ERROR, "Could not send the action asynchronously", exception);
                    try { Database.rollback(); } catch (@Nonnull SQLException exc) { Synchronizer.LOGGER.log(Level.WARNING, "Could not rollback", exc); }
                }
            }
        }.start();
        
        final @Nonnull FutureTask<RequestAudit> task;
        task = new FutureTask<RequestAudit>(new Callable<RequestAudit>() {
            @Override
            @Committing
            public @Nullable RequestAudit call() throws Exception {
                try {
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
                }
            }
        });
        task.run();
        
        try {
            return task.get();
        } catch (@Nonnull InterruptedException | ExecutionException exception) {
            Synchronizer.LOGGER.log(Level.ERROR, "Could not execute the action asynchronously", exception);
            throw new PacketException(PacketError.INTERNAL, "The action could not be executed asynchronously.", exception);
        }
    }
    
}
