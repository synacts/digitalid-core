package ch.virtualid.server;

import ch.virtualid.handler.ExternalAction;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Pushes the external actions to their recipients (and retries on failure).
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Pusher extends Thread {
    
    public static void send(@Nonnull ExternalAction action) throws SQLException {
        
    }
    
}
