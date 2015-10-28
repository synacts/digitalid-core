package net.digitalid.service.core.dataservice;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class  implements a data service to export and import host tables on {@link Host hosts}.
 * 
 * @see ClientTable
 * @see SiteTable
 */
@Immutable
abstract class HostTableImplementation<M extends DelegatingHostDataServiceImplementation> extends ClientTableImplementation<M> implements HostDataService {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Dump Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the dump type of this table.
     */
    private final @Nonnull @Loaded SemanticType dumpType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getDumpType() {
        return dumpType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host table implementation with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new table.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    protected HostTableImplementation(@Nonnull M module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType) {
        super(module, name);
        
        this.dumpType = dumpType;
        
        module.registerHostDataService(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Data –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public abstract @Nonnull Block exportAll(@Nonnull Host host) throws AbortException;
    
    @Locked
    @Override
    @NonCommitting
    public abstract void importAll(@Nonnull Host host, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException;
    
}
