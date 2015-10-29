package net.digitalid.service.core.dataservice;

import javax.annotation.Nonnull;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.configuration.Database;

/**
 * Client modules are only used on {@link Client clients}.
 * 
 * @see HostModule
 * @see SiteModule
 */
public class ClientModule extends DelegatingClientDataServiceImplementation {

    ClientModule(@Nonnull Service service, @Nonnull @Validated String name) {
        super(service, name);
        
        service.registerClientDataService(this);
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
}
