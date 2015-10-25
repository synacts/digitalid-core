package net.digitalid.service.core.data;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.host.Host;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models a database table that can be exported and imported on {@link Host hosts}.
 * 
 * @see StateTable
 */
@Immutable
public abstract class HostTable extends ClientTable implements HostData {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Module –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull HostModule getModule() {
        return (HostModule) super.getModule();
    }
    
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
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param dumpType the dump type of the new table.
     * 
     * @require !(module instanceof Service) : "The module is not a service.";
     */
    protected HostTable(@Nonnull HostModule module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType dumpType) {
        super(module, name);
        
        this.dumpType = dumpType;
        
        module.register(this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Sites –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final boolean isForHosts() {
        return true;
    }
    
    @Pure
    @Override
    public boolean isForClients() {
        return false;
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
