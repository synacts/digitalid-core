package net.digitalid.service.core.data;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.host.Host;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This interface models a collection of data that can be exported and imported on {@link Host hosts}.
 * 
 * @see StateData
 * @see HostTable
 * @see HostModule
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public interface HostData extends ClientData {
    
    /**
     * Returns the dump type of this data collection.
     * 
     * @return the dump type of this data collection.
     */
    @Pure
    public @Nonnull @Loaded SemanticType getDumpType();
    
    /**
     * Exports this data collection as a block.
     * 
     * @param host the host which is exported.
     * 
     * @return this data collection as a block.
     * 
     * @ensure return.getType().equals(getDumpType()) : "The returned block has the dump type of this data collection.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull @NonEncoding Block exportAll(@Nonnull Host host) throws SQLException;
    
    /**
     * Imports this data collection for the given host from the given block.
     * 
     * @param host the host for whom this data collection is to be imported.
     * @param block the block containing the data of this collection.
     * 
     * @require block.getType().isBasedOn(getDumpType()) : "The block is based on the dump type of this data collection.";
     */
    @Locked
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull @NonEncoding Block block) throws AbortException, PacketException, ExternalException, NetworkException;
    
}
