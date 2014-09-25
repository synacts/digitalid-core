package ch.virtualid.pusher;

import ch.virtualid.handler.ExternalAction;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Pushes the external actions to their recipients (and retries on failure).
 * 
 * TODO: Only retries if the connection could not be established. Otherwise an external action is created, signed and added to the internal audit.
 * 
 * @see PushFailed
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Pusher extends Thread {
    
    public static void send(@Nonnull ExternalAction action) throws SQLException {
        
    }
    
    // TODO: Make sure that failed pushs are signed and audited but not transmitted.
    
}
