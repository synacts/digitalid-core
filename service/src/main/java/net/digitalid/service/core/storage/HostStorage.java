package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.annotations.NonEncoding;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.service.core.site.host.Host;

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
    public void importAll(@Nonnull Host host, @Nonnull @NonEncoding Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
}
