package net.digitalid.core.pusher;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.handler.ExternalAction;

/**
 * Pushes the external actions to their recipients (and retries on failure).
 * 
 * TODO: Only retries if the connection could not be established. Otherwise an external action is created, signed and added to the internal audit.
 * 
 * @see PushFailed
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public final class Pusher extends Thread {
    
    @NonCommitting
    public static void send(@Nonnull ExternalAction action) throws SQLException {
        System.out.println("Push '" + action + "'."); // TODO: Write a real implementation!
    }
    
    // TODO: Make sure that failed pushs are signed and audited but not transmitted.
    
}
