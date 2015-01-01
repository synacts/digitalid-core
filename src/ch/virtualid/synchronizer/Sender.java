package ch.virtualid.synchronizer;

import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.io.Level;
import ch.virtualid.packet.Response;
import ch.virtualid.util.ReadonlyList;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * A sender sends {@link InternalAction internal actions} asynchronously.
 * 
 * @see Synchronizer
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Sender implements Runnable {
    
    /**
     * Stores the methods which are sent by this sender.
     * 
     * @invariant methods.isFrozen() : "The list of methods is frozen.";
     * @invariant methods.isNotEmpty() : "The list of methods is not empty.";
     * @invariant methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @invariant Method.areSimilar(methods) : "All methods are similar and belong to a non-host.";
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
     * @require Method.areSimilar(methods) : "All methods are similar and belong to a non-host.";
     */
    public Sender(@Nonnull ReadonlyList<Method> methods) {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        assert methods.doesNotContainNull() : "The list of methods does not contain null.";
        assert Method.areSimilar(methods) : "All methods are similar and belong to a non-host.";
        
        this.methods = methods;
    }
    
    /**
     * Sends the methods of this sender.
     */
    @Override
    public void run() {
        try {
            final int size = methods.size();
            final @Nonnull Response response = Method.send(methods, null); // TODO: Include audit.
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
    
}
