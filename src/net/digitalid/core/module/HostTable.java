package net.digitalid.core.module;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This class mo.
 * 
 * @see StateTable
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class HostTable<T extends HostTable<T>> extends Table<T> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Table Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the type of this table.
     */
    private final @Nonnull @Loaded SemanticType tableType;
    
    /**
     * Returns the type of this table.
     * 
     * @return the type of this table.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getTableType() {
        return tableType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new host table with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table.
     * @param tableType the type of the new table.
     */
    protected HostTable(@Nonnull Module<T> module, @Nonnull @Validated String name, @Nonnull @Loaded SemanticType tableType) {
        super(module, name);
        
        this.tableType = tableType;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Export and Import –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Exports this table encoded as a block.
     * 
     * @param host the host which is exported.
     * 
     * @return this table encoded as a block.
     * 
     * @ensure return.getType().equals(getTableType()) : "The returned block has the type of this table.";
     */
    @Pure
    @Locked
    @NonCommitting
    public abstract @Nonnull Block exportAll(@Nonnull Host host) throws SQLException;
    
    /**
     * Imports this table for the given host from the given block.
     * 
     * @param host the host for whom this table is to be imported.
     * @param block the block containing the data of this table.
     * 
     * @require block.getType().isBasedOn(getTableType()) : "The block is based on the type of this table.";
     */
    @Locked
    @NonCommitting
    public abstract void importAll(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
    
}
