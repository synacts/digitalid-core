package net.digitalid.service.core.storage;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.declaration.Declaration;
import net.digitalid.utility.database.site.Site;
import net.digitalid.utility.database.table.SpecificTable;

/**
 * This class implements a database table that can be created and deleted on {@link Client clients} and {@link Host hosts}.
 * 
 * @see ClientTable
 * @see HostTableImplementation
 */
@Immutable
abstract class ClientTableImplementation<M extends DelegatingClientStorageImplementation> extends SpecificTable implements ClientStorage {
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return module.getService();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new table implementation with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param declaration the declaration of the new table.
     */
    protected ClientTableImplementation(@Nonnull M module, @Nonnull @Validated String name, @Nonnull Declaration declaration) {
        super(module.getName() + "_" + name, declaration);
        
        this.module = module;
    }
    
    /* -------------------------------------------------- Tables -------------------------------------------------- */
    
    /**
     * Returns whether this table is for the given site.
     * 
     * @param site the site for which to check the query.
     * 
     * @return whether this table is for the given site.
     */
    @Pure
    protected abstract boolean isTableFor(@Nonnull Site site);
    
    @Locked
    @Override
    @NonCommitting
    public final void createTables(@Nonnull Site site) throws AbortException {
        try {
            if (isTableFor(site)) { create(site); }
        } catch (@Nonnull SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
    @Locked
    @Override
    @NonCommitting
    public final void deleteTables(@Nonnull Site site) throws AbortException {
        try {
            if (isTableFor(site)) { delete(site); }
        } catch (@Nonnull SQLException exception) {
            throw AbortException.get(exception);
        }
    }
    
}
