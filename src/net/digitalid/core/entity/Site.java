package net.digitalid.core.entity;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.client.Client;
import net.digitalid.core.database.Database;
import net.digitalid.core.host.Host;

/**
 * This class provides an interface so that the same code works on both {@link Host hosts} and {@link Client clients}.
 * 
 * @see Host
 * @see Client
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Site {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Prefix –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether the given prefix is valid.
     * 
     * @param prefix the prefix to be checked.
     * 
     * @return whether the given prefix is valid.
     */
    @Pure
    public static boolean isValidPrefix(@Nonnull String prefix) {
        return prefix.length() <= 40 && Database.getConfiguration().isValidIdentifier(prefix);
    }
    
    /**
     * Stores the prefix of the site-specific database tables.
     */
    private final @Nonnull @Validated String prefix;
    
    /**
     * Returns the prefix of the site-specific database tables.
     * 
     * @return the prefix of the site-specific database tables.
     */
    @Pure
    @Override
    public final @Nonnull @Validated String toString() {
        return prefix;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new site with the given prefix.
     * 
     * @param prefix the prefix of the site-specific database tables.
     */
    protected Site(@Nonnull @Validated String prefix) {
        assert isValidPrefix(prefix) : "The prefix is valid.";
        
        this.prefix = prefix;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Reference –––––––––––––––––––––––––––––––––––––––––––––––––– */
        
    /**
     * Returns the foreign key referenced by the entity column.
     * 
     * @return the foreign key referenced by the entity column.
     */
    @Pure
    public abstract @Nonnull String getEntityReference();
    
}
