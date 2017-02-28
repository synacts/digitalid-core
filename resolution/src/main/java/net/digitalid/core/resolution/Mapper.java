package net.digitalid.core.resolution;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.interfaces.Database;

import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identity.Identity;

/**
 * This class caches identities in {@link Map maps}.
 */
@Mutable
@GenerateSubclass
public abstract class Mapper {
    
    /* -------------------------------------------------- Maps -------------------------------------------------- */
    
    /**
     * Maps numbers onto identities by caching the corresponding entries from the database.
     */
    private final @Nonnull Map<@Nonnull Long, @Nonnull Identity> keys = new ConcurrentHashMap<>();
    
    /**
     * Maps identifiers onto identities by caching the corresponding entries from the database.
     */
    private final @Nonnull Map<@Nonnull Identifier, @Nonnull Identity> identifiers = new ConcurrentHashMap<>();
    
    /* -------------------------------------------------- Retrievals -------------------------------------------------- */
    
    /**
     * Returns the identity with the given key or null if no such identity is mapped.
     */
    @Pure
    public @Nullable Identity getIdentity(long key) {
        return keys.get(key);
    }
    
    /**
     * Returns the identity with the given identifier or null if no such identity is mapped.
     */
    @Pure
    public @Nullable Identity getIdentity(@Nonnull Identifier identifier) {
        return identifiers.get(identifier);
    }
    
    /* -------------------------------------------------- Modifications -------------------------------------------------- */
    
    /**
     * Adds the given identity to the local maps.
     */
    @Impure
    public void map(@Nonnull Identity identity) {
        keys.put(identity.getKey(), identity);
        identifiers.put(identity.getAddress(), identity);
        Log.debugging("The identity of $ was mapped.", identity.getAddress().getString());
    }
    
    /**
     * Adds the given identity to the local maps after committing the current transaction successfully.
     * If the current transaction is rolled back for whatever reason, then the identity is not mapped.
     */
    @Impure
    public void mapAfterCommit(@Nonnull Identity identity) {
        Database.instance.get().runAfterCommit(() -> map(identity));
    }
    
    /**
     * Removes the given identity from the local maps.
     */
    @Impure
    public void unmap(@Nonnull Identity identity) {
        keys.remove(identity.getKey());
        identifiers.remove(identity.getAddress());
        Log.debugging("The identity of $ was unmapped.", identity.getAddress().getString());
    }
    
    /**
     * Clears all entries from the local maps.
     */
    @Impure
    public void unmapAll() {
        keys.clear();
        identifiers.clear();
    }
    
}
