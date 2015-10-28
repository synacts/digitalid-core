package net.digitalid.service.core.dataservice;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.site.Site;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * Implements the smallest subset of data service functionalities. The implemented data service is used on clients and hosts.
 * 
 * @see DelegatingHostDataServiceImplementation
 */
class DelegatingClientDataServiceImplementation implements ClientDataService {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Service –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the service to which this module belongs.
     */
    private final @Nonnull Service service;
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return service;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Name –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the name of this module.
     */
    private final @Nonnull @Validated String name;
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName() {
        return name;
    }
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName(@Nonnull Site site) {
        return site + name;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */

    /**
     * Creates a new client module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    DelegatingClientDataServiceImplementation(@Nullable Service service, @Nonnull @Validated String name) {
        if (this instanceof Service) {
            this.service = (Service) this;
            this.name = "_" + name;
        } else if (service != null) {
            this.service = service;
            this.name = service.getName() + "_" + name;
        } else {
            throw new ShouldNeverHappenError("Only the service class should call this constructor with null.");
        }
        
        assert ClientModule.isValidName(this.name) : "The name is valid.";
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Tables –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the tables of this module.
     */
    protected final @Nonnull @NonNullableElements @NonFrozen FreezableList<ClientDataService> tables = FreezableLinkedList.get();
    
    /**
     * Returns the tables of this module.
     * 
     * @return the tables of this module.
     */
    @Pure
    public @Nonnull @NonNullableElements ReadOnlyList<ClientDataService> getTables() {
        return tables;
    }
    
    /**
     * Registers the given table at this module.
     * 
     * @param table the table to be registered.
     */
    final void registerClientDataService(@Nonnull ClientDataService table) {
        tables.add(table);
    }
    

    @Locked
    @Override
    @NonCommitting
    public final void createTables(@Nonnull Site client) throws SQLException {
        for (final @Nonnull ClientDataService table : getTables()) {
            table.createTables(client);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws SQLException {
        for (final @Nonnull ClientDataService table : getTables()) {
            table.deleteTables(site);
        }
    }
}
