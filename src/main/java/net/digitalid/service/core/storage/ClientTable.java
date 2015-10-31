package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class models a database table that can be created and removed on {@link Client clients}.
 * 
 * @see HostTable
 * @see SiteTable
 */
@Immutable
public abstract class ClientTable extends ClientTableImplementation<ClientModule> {
    
    /**
     * Creates a new table with the given module and name.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     */
    protected ClientTable(@Nonnull ClientModule module, @Nonnull @Validated String name) {
        super(module, name);
        
        module.registerClientDataService(this);
    }
    
}
