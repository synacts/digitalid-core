package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.packet.PacketErrorCode;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.database.annotations.OnMainThread;

/**
 * Site modules are used on both {@link Host hosts} and {@link Client clients}.
 * 
 * @see ClientModule
 * @see HostModule
 */
public class SiteModule extends DelegatingSiteStorageImplementation {

    /**
     * Creates a new site module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     */
    SiteModule(@Nonnull Service service, @Nonnull @Validated String name) {
        super(service, name);

        service.registerSiteDataService(this);
        modules.put(super.getStateType(), this);
    }

    /**
     * Returns a new site module with the given service and name.
     * 
     * @param service the service to which the new module belongs.
     * @param name the name of the new module without any prefix.
     * 
     * @return a new site module with the given service and name.
     */
    @Pure
    @OnMainThread
    public static @Nonnull SiteModule get(@Nonnull Service service, @Nonnull @Validated String name) {
        return new SiteModule(service, name);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Modules –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Maps the modules that exist on this server from their state type.
     */
    private static final @Nonnull FreezableMap<SemanticType, SiteModule> modules = FreezableLinkedHashMap.get();
    
    /**
     * Returns the module whose state type matches the given type.
     * 
     * @param stateType the state type of the desired module.
     * 
     * @return the module whose state type matches the given type.
     */
    @Pure
    public static @Nonnull SiteModule getModule(@Nonnull SemanticType stateType) throws PacketException {
        final @Nullable SiteModule module = modules.get(stateType);
        if (module != null) return module;
        throw new PacketException(PacketErrorCode.SERVICE, "There exists no module with the state type " + stateType.getAddress() + ".");
    }
}
