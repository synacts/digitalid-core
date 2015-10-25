package net.digitalid.service.core.concept.property;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.OnlyForClients;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.data.Service;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.SignatureWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * Description.
 */
@Immutable
public abstract class ConceptPropertyInternalAction extends InternalAction {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code old.time@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType OLD_TIME = SemanticType.map("old.time@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code new.time@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType NEW_TIME = SemanticType.map("new.time@core.digitalid.net").load(Time.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Service –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    private final @Nonnull Service service;
    
    @Pure
    @Override
    public final @Nonnull Service getService() {
        return service;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @OnlyForClients
    protected ConceptPropertyInternalAction(@Nonnull Role role, @Nonnull Service service) throws AbortException {
        super(role, service.getRecipient(role));
        
        this.service = service;
    }
    
    protected ConceptPropertyInternalAction(@Nonnull Entity<?> entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Service service) throws AbortException, PacketException, ExternalException, NetworkException {
        super(entity, signature, recipient);
        
        this.service = service;
    }
    
}
