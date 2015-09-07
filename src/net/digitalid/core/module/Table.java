package net.digitalid.core.module;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Site;

/**
 * This class models a database table.
 * 
 * @see HostTable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class Table<T extends Table<T>> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the module to which this table belongs.
     */
    private final @Nonnull Module<T> module;
    
    /**
     * Returns the module to which this table belongs.
     * 
     * @return the module to which this table belongs.
     */
    @Pure
    public final @Nonnull Module<T> getModule() {
        return module;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Name –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether the given name is valid.
     * 
     * @param name the name to be checked.
     * 
     * @return whether the given name is valid.
     */
    @Pure
    public final boolean isValid(@Nonnull String name) {
        return name.length() <= 22 && Database.getConfiguration().isValidIdentifier(name);
    }
    
    /**
     * Stores the name of this table.
     */
    private final @Nonnull @Validated String name;
    
    /**
     * Returns the name of this table.
     * 
     * @return the name of this table.
     */
    @Pure
    public final @Nonnull @Validated String getName() {
        return name;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new table with the given module and name.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table.
     */
    @SuppressWarnings("unchecked")
    protected Table(@Nonnull Module<T> module, @Nonnull @Validated String name) {
        this.module = module;
        this.name = name;
        
        module.register((T) this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Creation and Deletion –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates this table for the given site.
     * 
     * @param site the site for which to create this table.
     */
    @Locked
    @NonCommitting
    protected abstract void create(@Nonnull Site site) throws SQLException;
    
    /**
     * Deletes this table for the given site.
     * 
     * @param site the site for which to delete this table.
     */
    @Locked
    @NonCommitting
    protected void delete(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + name);
        }
    }
    
}
