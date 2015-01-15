package ch.virtualid.service;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Role;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
     * Stores the semantic type {@code @virtualid.ch}.
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
    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalNonHostIdentity subject) {
        return subject.getAddress().getHostIdentifier();
    }
    
    
    /**
     * Stores the semantic type {@code module@virtualid.ch}.
     */
    private static final @Nonnull SemanticType MODULE = SemanticType.create("module@virtualid.ch").load(Service.MODULES);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE;
    }
    
    
    /**
     * Stores the semantic type {@code state@virtualid.ch}.
     */
    public static final @Nonnull SemanticType STATE = SemanticType.create("state@virtualid.ch").load(Service.STATES);
    
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
