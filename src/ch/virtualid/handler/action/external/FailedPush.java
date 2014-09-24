package ch.virtualid.handler.action.external;

import ch.virtualid.server.Pusher;

/**
 * An action of this type is added to the audit if the {@link Pusher} failed to send an external action.
 * 
 * @see Pusher
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class FailedPush extends CoreServiceExternalAction {
    
    public FailedPush() {
        
    }
    
    // TODO: Maybe it's possible to rewrite the send method so that the action is signed and audited but not transmitted.
    
}
