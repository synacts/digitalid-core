package ch.virtualid.database;

import ch.virtualid.client.Client;
import ch.virtualid.server.Host;
import javax.annotation.Nonnull;

/**
 * This class provides an interface so that the same code works on both {@link Client clients} and {@link Host hosts}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Site {
    
    /**
     * Stores the prefix of the site-specific database tables.
     */
    private final @Nonnull String prefix;
    
    /**
     * Creates a new site with the given prefix.
     * 
     * @param prefix the prefix of the site-specific database tables.
     */
    protected Site(@Nonnull String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * Returns the foreign key referenced by the entity column.
     * 
     * @return the foreign key referenced by the entity column.
     */
    public abstract @Nonnull String getReference();
    
    /**
     * Returns the prefix of the site-specific database tables.
     * 
     * @return the prefix of the site-specific database tables.
     */
    @Override
    public final @Nonnull String toString() {
        return prefix;
    }
    
}
