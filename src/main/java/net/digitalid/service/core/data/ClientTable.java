package net.digitalid.service.core.data;

import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Site;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a database table with the least requirements.
 * 
 * @see HostTable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class ClientTable implements ClientData {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the module to which this table belongs.
     */
    private final @Nonnull ClientModule module;
    
    /**
     * Returns the module to which this table belongs.
     * 
     * @return the module to which this table belongs.
     */
    @Pure
    public @Nonnull ClientModule getModule() {
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
     * Stores the name of this table.
     */
    private final @Nonnull @Validated String name;
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName() {
        return name; // TODO: Check whether the name consists of the service, module and property separated by an underline.
    }
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName(@Nonnull Site site) {
        return site + name;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new table with the given module and name.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    protected ClientTable(@Nonnull ClientModule module, @Nonnull @Validated String name) {
        assert !(module instanceof Service) : "The module is not a service.";
        
        this.module = module;
        this.name = module.getName() + "_" + name;
        
        module.register(this);
        
        assert isValidName(this.name) : "The name is valid.";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Sites –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean isForHosts() {
        return false;
    }
    
    @Pure
    @Override
    public boolean isForClients() {
        return true;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Locked
    @Override
    @NonCommitting
    public abstract void createTables(@Nonnull Site site) throws SQLException;
    
    @Locked
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + name);
        }
    }
    
}
