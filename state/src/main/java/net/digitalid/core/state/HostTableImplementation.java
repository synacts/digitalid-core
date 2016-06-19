package net.digitalid.core.state;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.Loaded;

/**
 * This class implements a database table that can be exported and imported on {@link Host hosts}.
 * 
 * @see HostTable
 * @see SiteTableImplementation
 */
@Immutable
abstract class HostTableImplementation<M extends DelegatingHostStorageImplementation> extends ClientTableImplementation<M> implements HostStorage {
    
    /* -------------------------------------------------- Dump Type -------------------------------------------------- */
    
    /**
     * Stores the dump type of this host table.
     */
    private final @Nonnull @Loaded SemanticType dumpType;
    
    @Pure
    @Override
    public final @Nonnull @Loaded SemanticType getDumpType() {
        return dumpType;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new host table implementation with the given parameters.
     * 
     * @param module the module to which the new table belongs.
     * @param name the name of the new table (unique within the module).
     * @param declaration the declaration of the new table.
     * @param dumpType the dump type of the new host table.
     */
    protected HostTableImplementation(@Nonnull M module, @Nonnull @Validated String name, @Nonnull Declaration declaration, @Nonnull @Loaded SemanticType dumpType) {
        super(module, name, declaration);
        
        this.dumpType = dumpType;
    }
    
    /* -------------------------------------------------- Dumps -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public abstract @Nonnull Block exportAll(@Nonnull Host host) throws DatabaseException;
    
    @Locked
    @Override
    @NonCommitting
    public abstract void importAll(@Nonnull Host host, @Nonnull Block block) throws ExternalException;
    
}
