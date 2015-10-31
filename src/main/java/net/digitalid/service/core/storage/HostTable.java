package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Validated;

/**
 * This class models a database table that can be exported and imported on {@link Host hosts}.
 * 
 * @see ClientTable
 * @see SiteTable
 */
@Immutable
public abstract class HostTable extends HostTableImplementation<HostModule> {
    
    /**
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new table.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    protected HostTable(@Nonnull HostModule module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType) {
        super(module, name, dumpType);
        
        module.registerHostDataService(this);
    }
    
}
