package net.digitalid.core.module;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * Host modules are only used on the {@link Host host}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface HostModule extends Module {
    
    /**
     * Returns the format of this module.
     * 
     * @return the format of this module.
     * 
     * @ensure return.isLoaded() : "The type declaration is loaded.";
     */
    @Pure
    public @Nonnull SemanticType getModuleFormat();
    
    /**
     * Returns this module encoded as a block.
     * 
     * @param host the host which is exported.
     * 
     * @return this module encoded as a block.
     * 
     * @ensure return.getType().equals(getModuleFormat()) : "The returned block has the format of this module.";
     */
    @Pure
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException;
    
    /**
     * Imports this module for the given host from the given block.
     * 
     * @param host the host for whom this module is to be imported.
     * @param block the block containing the data of this module.
     * 
     * @require block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
     */
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
    
}
