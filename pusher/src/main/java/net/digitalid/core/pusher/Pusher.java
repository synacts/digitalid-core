package net.digitalid.core.pusher;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.handler.method.action.ExternalAction;

/**
 * Pushes the external actions to their recipients (and retries on failure).
 * 
 * TODO: Only retries if the connection could not be established. Otherwise an external action is created, signed and added to the internal audit.
 * 
 * @see PushFailed
 */
@Utility
public abstract class Pusher extends Thread {
    
    @NonCommitting
    @PureWithSideEffects
    public static void send(@Nonnull ExternalAction action) throws DatabaseException {
        Log.error("The action '" + action + "' should have been pushed but this is not implemented yet."); // TODO: Write a real implementation!
    }
    
    // TODO: Make sure that failed pushs are signed and audited but not transmitted.
    
}
