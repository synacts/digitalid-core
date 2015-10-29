package net.digitalid.service.core.dataservice;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.site.Site;

/**
 * This interface models a collection of data with the least requirements.
 * 
 * @see HostDataService
 * @see ClientTableImplementation
 * @see DelegatingClientDataServiceImplementation
 */
interface ClientDataService {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Fields –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the service to which this data collection belongs.
     * 
     * @return the service to which this data collection belongs.
     */
    @Pure
    public @Nonnull Service getService();
    
    /**
     * Returns the name of this data collection.
     * 
     * @return the name of this data collection.
     */
    @Pure
    public @Nonnull @Validated String getName();
    
    /**
     * Returns the name of this data collection with the prefix of the given site.
     * 
     * @param site the site whose prefix is to be used for the returned name.
     * 
     * @return the name of this data collection with the prefix of the given site.
     */
    @Pure
    public @Nonnull @Validated String getName(@Nonnull Site site);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates the database tables of this data collection for the given site.
     * 
     * @param site the site for which to create the database tables of this data collection.
     */
    @Locked
    @NonCommitting
    public void createTables(@Nonnull Site site) throws SQLException;

    /**
     * Deletes the database tables of this data collection for the given site.
     * 
     * @param site the site for which to delete the database tables of this data collection.
     */
    @Locked
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException;

    

}
