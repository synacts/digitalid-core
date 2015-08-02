package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.entity.Role;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;

/**
 * This class models the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class CoreService extends Service {
    
    /**
     * Creates a new core service.
     */
    private CoreService() {
        super("Core Service", "1.0");
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return SERVICE;
    }
    
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = Identity.IDENTIFIER;
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
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
    
    
    /**
     * Stores the semantic type {@code module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.map("module@core.digitalid.net").load(Service.MODULES);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    
    /**
     * Stores the semantic type {@code state@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType STATE = SemanticType.map("state@core.digitalid.net").load(Service.STATES);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE;
    }
    
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull CoreService SERVICE = new CoreService();
    
}
