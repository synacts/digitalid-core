package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
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
public abstract class Site extends Instance {
    
    /**
     * Stores the aspect of a new site being created.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(Site.class, "created");
    
    /**
     * Stores the aspect of a site being deleted.
     */
    public static final @Nonnull Aspect DELETED = new Aspect(Site.class, "deleted");
    
    
    /**
     * Stores the prefix of the site-specific database tables.
     * 
     * @invariant prefix.length() <= 41 : "The prefix has at most 41 characters.";
     */
    private final @Nonnull String prefix;
    
    /**
     * Creates a new site with the given prefix.
     * 
     * @param prefix the prefix of the site-specific database tables.
     * 
     * @require prefix.length() <= 40 : "The prefix has at most 40 characters.";
     */
    protected Site(@Nonnull String prefix) throws SQLException {
        assert prefix.length() <= 40 : "The prefix has at most 40 characters.";
        
        this.prefix = prefix + "_";
    }
    
    /**
     * Returns the prefix of the site-specific database tables.
     * 
     * @return the prefix of the site-specific database tables.
     * 
     * @invariant return.length() <= 41 : "The prefix has at most 41 characters.";
     */
    @Pure
    @Override
    public final @Nonnull String toString() {
        return prefix;
    }
        
    /**
     * Returns the foreign key referenced by the entity column.
     * 
     * @return the foreign key referenced by the entity column.
     */
    @Pure
    public abstract @Nonnull String getEntityReference();
    
}
