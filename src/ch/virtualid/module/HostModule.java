package ch.virtualid.module;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.host.Host;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Host modules are only used on the {@link Host host}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException;
    
    /**
     * Imports this module for the given host from the given block.
     * 
     * @param host the host for whom this module is to be imported.
     * @param block the block containing the data of this module.
     * 
     * @require block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
     */
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
    
}
