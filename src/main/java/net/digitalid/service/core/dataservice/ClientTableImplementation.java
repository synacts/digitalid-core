package net.digitalid.service.core.dataservice;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.site.Site;

/**
 * This class implements a data service to create and delete database table. 
 * The functionality is required on hosts and clients.
 * 
 * @see HostTable
 * @see SiteTable
 */
@Immutable
abstract class ClientTableImplementation<M extends DelegatingClientDataServiceImplementation> implements ClientDataService {
    
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
    public @Nonnull M getModule() {
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
    public final boolean isValidName(@Nonnull String name) {
        return name.length() <= 22 && name.startsWith("_") && name.length() > 1 && Database.getConfiguration().isValidIdentifier(name);
    }
    
    /**
     * Stores the name of this table, which has to be unique within the module.
     */
    private final @Nonnull @Validated String name;
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName() {
        return name; // TODO: Check whether the name consists of the service, module and property separated by an underline.
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
        
        module.registerClientDataService(this);
        
        assert isValidName(this.name) : "The name is valid.";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public abstract void createTables(@Nonnull Site client) throws SQLException;
    
    @Locked
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site client) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + client + name);
        }
    }
    
}
