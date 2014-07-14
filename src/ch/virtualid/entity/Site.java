package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.module.Module;
import ch.virtualid.server.Host;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class provides an interface so that the same code works on both {@link Host hosts} and {@link Client clients}.
 * 
 * @see Host
 * @see Client
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
    protected Site(@Nonnull String prefix) throws SQLException {
        this.prefix = prefix;
        
        Module.initialize(this);
    }
    
    /**
     * Returns the foreign key referenced by the entity column.
     * 
     * @return the foreign key referenced by the entity column.
     */
    @Pure
    public abstract @Nonnull String getReference();
    
    /**
     * Returns the prefix of the site-specific database tables.
     * 
     * @return the prefix of the site-specific database tables.
     */
    @Pure
    @Override
    public final @Nonnull String toString() {
        return prefix;
    }
        
}
