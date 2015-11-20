package net.digitalid.service.core.action.pusher;

import javax.annotation.Nonnull;
import net.digitalid.service.core.handler.ExternalAction;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.system.logger.Log;

/**
 * Pushes the external actions to their recipients (and retries on failure).
 * 
 * TODO: Only retries if the connection could not be established. Otherwise an external action is created, signed and added to the internal audit.
 * 
 * @see PushFailed
 */
public final class Pusher extends Thread {
    
    @NonCommitting
    public static void send(@Nonnull ExternalAction action) throws DatabaseException {
        Log.error("The action '" + action + "' should have been pushed but this is not implemented yet."); // TODO: Write a real implementation!
    }
    
    // TODO: Make sure that failed pushs are signed and audited but not transmitted.
    
}
