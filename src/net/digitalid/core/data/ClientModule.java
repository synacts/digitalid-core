package net.digitalid.core.data;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.client.Client;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.errors.ShouldNeverHappenError;
import net.digitalid.core.host.Host;

/**
 * Client modules are only used on {@link Client clients}.
 * 
 * @see HostModule
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public class ClientModule implements ClientData {
    
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
     * Stores the name of this module.
     */
    private final @Nonnull @Validated String name;
    
    @Pure
    @Override
    public final @Nonnull @Validated String getName() {
        return name;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new client module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    protected ClientModule(@Nullable Service service, @Nonnull @Validated String name) {
        if (this instanceof Service) {
            this.service = (Service) this;
            this.name = "_" + name;
        } else if (service != null) {
            this.service = service;
            this.name = service.getName() + "_" + name;
            service.register(this);
        } else {
            throw new ShouldNeverHappenError("Only the service class should call this constructor with null.");
        }
        
        assert isValidName(this.name) : "The name is valid.";
    }
    
    /**
     * Returns a new client module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     * 
     * @return a new client module with the given service and name.
     */
    @Pure
    public static @Nonnull ClientModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new ClientModule(service, name);
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
    
    /**
     * Stores the tables of this module.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<ClientData> tables = FreezableLinkedList.get();
    
    /**
     * Returns the tables of this module.
     * 
     * @return the tables of this module.
     */
    @Pure
    public final @Nonnull @NonNullableElements ReadOnlyList<ClientData> getTables() {
        return tables;
    }
    
    /**
     * Registers the given table at this module.
     * 
     * @param table the table to be registered.
     */
    final void register(@Nonnull ClientData table) {
        tables.add(table);
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void createTables(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull ClientData table : tables) {
                if (table.isForHosts()) table.createTables(site);
            }
        } else if (site instanceof Client) {
            for (final @Nonnull ClientData table : tables) {
                if (table.isForClients()) table.createTables(site);
            }
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws SQLException {
        if (site instanceof Host) {
            for (final @Nonnull ClientData table : tables) {
                if (table.isForHosts()) table.deleteTables(site);
            }
        } else if (site instanceof Client) {
            for (final @Nonnull ClientData table : tables) {
                if (table.isForClients()) table.deleteTables(site);
            }
        }
    }
    
}
