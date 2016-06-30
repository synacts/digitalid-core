package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.client.Client;
import net.digitalid.core.host.Host;

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
