package ch.virtualid.module;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.query.internal.StateQuery;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;

/**
 * This class represents the core service.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class CoreService extends Service {
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull CoreService SERVICE = new CoreService();
    
    /**
     * Creates a new core service.
     */
    private CoreService() {
        super("Core Service", "1.0");
    }
    
    
    /**
     * Stores the semantic type {@code @virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("@virtualid.ch").load(Identity.IDENTIFIER);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
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
    
    @Pure
    @Override
    public @Nonnull StateQuery getInternalQuery(@Nonnull Role role) {
        return new StateQuery(role);
    }
    
}
