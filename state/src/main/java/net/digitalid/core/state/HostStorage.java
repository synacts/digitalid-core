package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;

import net.digitalid.database.annotations.transaction.Locked;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.annotations.NonEncoding;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.Loaded;

/**
 * This interface models a storage that can be exported and imported on {@link Host hosts}.
 * 
 * @see SiteStorage
 * @see HostTableImplementation
 * @see DelegatingHostStorageImplementation
 */
public interface HostStorage extends ClientStorage {
    
    /**
     * Returns the dump type of this storage.
     * 
     * @return the dump type of this storage.
     */
    @Pure
    public @Nonnull @Loaded SemanticType getDumpType();
    
    /**
     * Exports this storage as a block.
     * 
     * @param host the host which is exported.
     * 
     * @return this storage as a block.
     * 
     * @ensure return.getType().equals(getDumpType()) : "The returned block has the dump type of this storage.";
     */
    @Pure
    @Locked
    @NonCommitting
    public @Nonnull @NonEncoding Block exportAll(@Nonnull Host host) throws DatabaseException;
    
    /**
     * Imports this storage for the given host from the given block.
     * 
     * @param host the host for whom this storage is to be imported.
     * @param block the block containing the data of this storage.
     * 
     * @require block.getType().isBasedOn(getDumpType()) : "The block is based on the dump type of this storage.";
     */
    @Locked
    @NonCommitting
    public void importAll(@Nonnull Host host, @Nonnull @NonEncoding Block block) throws ExternalException;
    
}
