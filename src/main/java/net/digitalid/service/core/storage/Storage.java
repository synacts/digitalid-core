package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.request.RequestErrorCode;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableMap;

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
        if (storage == null) { throw new RequestException(RequestErrorCode.SERVICE, "There exists no site storage with the state type " + stateType.getAddress() + "."); }
        return storage;
    }
    
}
