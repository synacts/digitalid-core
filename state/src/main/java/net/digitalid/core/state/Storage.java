package net.digitalid.core.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.SemanticType;

/**
 * This class maps to all the site storages given their state type.
 */
@Stateless
public final class Storage {
    
    /**
     * Maps to all the site storages from their state type.
     */
    private static final @Nonnull FreezableMap<SemanticType, SiteStorage> storages = FreezableLinkedHashMap.get();
    
    /**
     * Registers the given storage with its state type.
     * 
     * @param storage the site storage to be registered.
     */
    static void register(@Nonnull SiteStorage storage) {
        storages.put(storage.getStateType(), storage);
    }
    
    /**
     * Returns the site storage whose state type matches the given type.
     * 
     * @param stateType the state type of the desired site storage.
     * 
     * @return the site storage whose state type matches the given type.
     */
    @Pure
    public static @Nonnull SiteStorage get(@Nonnull SemanticType stateType) throws RequestException {
        final @Nullable SiteStorage storage = storages.get(stateType);
        if (storage == null) { throw RequestException.get(RequestErrorCode.SERVICE, "There exists no site storage with the state type " + stateType.getAddress() + "."); }
        return storage;
    }
    
}
