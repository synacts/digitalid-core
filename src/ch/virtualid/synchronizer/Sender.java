package ch.virtualid.synchronizer;

import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.error.ErrorModule;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.io.Level;
import ch.virtualid.packet.Response;
import ch.virtualid.service.Service;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * A sender sends {@link InternalAction internal actions} asynchronously.
 * 
 * @see Synchronizer
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
final class Sender extends Thread {
    
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
                            try {
                                response.checkReply(i);
                            } catch (@Nonnull PacketException exception) {
                                Synchronizer.LOGGER.log(Level.WARNING, "Could not execute on the host", exception);
                                final @Nonnull InternalAction action = (InternalAction) methods.getNotNull(i);
                                
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
            } catch (@Nonnull SQLException e) {
                Synchronizer.LOGGER.log(Level.WARNING, "Could not rollback", exception);
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
    
}
