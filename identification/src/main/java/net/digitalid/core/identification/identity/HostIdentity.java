package net.digitalid.core.identification.identity;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.system.thread.annotations.MainThread;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.Category;
import net.digitalid.core.resolution.Mapper;

/**
 * This class models a host identity.
 */
@Immutable
public final class HostIdentity extends IdentityImplementation implements InternalIdentity {
    
    /* -------------------------------------------------- Digital ID Host Identity -------------------------------------------------- */
    
    /**
     * Maps the given identifier to a new host identity.
     * 
     * @param identifier the identifier which is to be mapped.
     */
    @MainThread
    private static @Nonnull HostIdentity map(@Nonnull HostIdentifier identifier) {
        Require.that(Threading.isMainThread()).orThrow("This method may only be called in the main thread.");
        
        try {
            return Mapper.mapHostIdentity(identifier);
        } catch (@Nonnull DatabaseException exception) {
            throw InitializationError.get("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Stores the host identity of {@code core.digitalid.net}.
     */
    public static final @Nonnull HostIdentity DIGITALID = HostIdentity.map(HostIdentifier.DIGITALID);
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Stores the address of this host identity.
     */
    private final @Nonnull HostIdentifier address;
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getAddress() {
        return address;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new host identity with the given key and address.
     * 
     * @param key the number that represents this identity.
     * @param address the address of the new host identity.
     */
    HostIdentity(long key, @Nonnull HostIdentifier address) {
        super(key);
        
        this.address = address;
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
        Mapper.unmap(this);
        throw exception;
    }
    
}
