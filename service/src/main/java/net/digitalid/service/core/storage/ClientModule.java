package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.validation.state.Validated;

import net.digitalid.service.core.site.client.Client;

/**
 * Client modules contain {@link ClientTable client tables} and are only used on {@link Client clients}.
 * 
 * @see HostModule
 * @see SiteModule
 */
@Immutable
public final class ClientModule extends DelegatingClientStorageImplementation {
    
    /**
     * Creates a new client module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    private ClientModule(@Nonnull Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        service.registerClientStorage(this);
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
    
}
