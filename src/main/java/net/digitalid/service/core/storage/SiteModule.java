package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;

/**
 * Site modules contain {@link SiteTable site tables} and are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @see ClientModule
 * @see HostModule
 */
@Immutable
public final class SiteModule extends DelegatingSiteStorageImplementation {
    
    /**
     * Creates a new site module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    private SiteModule(@Nonnull Service service, @Nonnull @Validated String name) {
        super(service, name);

        service.registerSiteStorage(this);
    }

    /**
     * Returns a new site module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     * 
     * @return a new site module with the given service and name.
     */
    @Pure
    @MainThread
    public static @Nonnull SiteModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new SiteModule(service, name);
    }
    
}
