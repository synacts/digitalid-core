package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.core.entity.Role;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.state.Service;

/**
 * This class models the core service.
 */
public final class CoreService extends Service {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new core service.
     */
    private CoreService() {
        super("core", Identity.IDENTIFIER, "Core Service", "1.0");
    }
    
    /* -------------------------------------------------- Singleton -------------------------------------------------- */
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull CoreService SERVICE = new CoreService();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
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
