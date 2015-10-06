package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.data.Service;
import net.digitalid.core.entity.Role;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.InternalPerson;

/**
 * This class models the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public final class CoreService extends Service {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new core service.
     */
    private CoreService() {
        super("core", Identity.IDENTIFIER, "Core Service", "1.0");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Singleton –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull CoreService SERVICE = new CoreService();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Recipient –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient(@Nonnull Role role) {
        return role.getIdentity().getAddress().getHostIdentifier();
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalPerson subject) {
        return subject.getAddress().getHostIdentifier();
    }
    
}
