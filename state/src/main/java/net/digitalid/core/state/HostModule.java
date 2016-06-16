package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.host.Host;

/**
 * Host modules contain {@link HostTable host tables} and are only used on {@link Host hosts}.
 * 
 * @see ClientModule
 * @see SiteModule
 */
@Immutable
public final class HostModule extends DelegatingHostStorageImplementation {

    /**
     * Creates a new host module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    @MainThread
    private HostModule(@Nonnull Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        service.registerHostStorage(this);
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
    @MainThread
    public static @Nonnull HostModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new HostModule(service, name);
    }
    
}