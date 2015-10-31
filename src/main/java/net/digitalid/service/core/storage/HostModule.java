package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.OnMainThread;

/**
 * Host modules are only used on {@link Host hosts}.
 * 
 * @see ClientModule
 * @see SiteModule
 */
public class HostModule extends DelegatingHostStorageImplementation {

    /**
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new table.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    HostModule(@Nonnull Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        service.registerHostDataService(this);
    }

    /**
     * Returns a new host module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     * 
     * @return a new host module with the given service and name.
     */
    @Pure
    @OnMainThread
    public static @Nonnull HostModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new HostModule(service, name);
    }
}
