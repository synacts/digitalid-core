package ch.virtualid.database;

import ch.virtualid.client.Client;
import ch.virtualid.server.Host;
import javax.annotation.Nonnull;

/**
 * Classes that need to initialize site-specific database tables should implement this interface.
 * 
 * @see Host
 * @see Client
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface Initializable {
    
    public void initialize(@Nonnull Site site);
    
}
