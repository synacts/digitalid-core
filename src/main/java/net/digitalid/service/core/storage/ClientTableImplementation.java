package net.digitalid.service.core.storage;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class implements a database table that can be created and deleted on {@link Client clients} and {@link Host hosts}.
 * 
 * @see ClientTable
 * @see HostTableImplementation
 */
@Immutable
abstract class ClientTableImplementation<M extends DelegatingClientStorageImplementation> implements ClientStorage {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the module to which this table belongs.
     */
    private final @Nonnull M module;
    
    /**
     * Returns the module to which this table belongs.
     * 
     * @return the module to which this table belongs.
     */
    @Pure
    public final @Nonnull M getModule() {
        return module;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Service –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return module.getService();
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
    public static boolean isValidName(@Nonnull String name) {
        return name.length() <= 22 && name.startsWith("_") && name.length() > 1 && Database.getConfiguration().isValidIdentifier(name);
    }
    
    /**
     * Stores the name of this table, which has to be unique within the module.
     */
    private final @Nonnull @Validated String name;
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName() {
        return name;
    }
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName(@Nonnull Site client) {
        return client + name;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new table implementation with the given module and name.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     */
    protected ClientTableImplementation(@Nonnull M module, @Nonnull @Validated String name) {
        this.module = module;
        this.name = module.getName() + "_" + name;
        
        assert isValidName(this.name) : "The name is valid.";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public abstract void createTables(@Nonnull Site site) throws AbortException;
    
    @Locked
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws AbortException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + name);
        } catch (@Nonnull SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
}
