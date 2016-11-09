package net.digitalid.core.handler;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;

/**
 * This interface is implemented by all handlers of the core service.
 * 
 * @see CoreService
 */
@Immutable
public interface CoreHandler<E extends Entity> extends Handler<E> {
    
    @Pure
    @Override
    public default @Nonnull Service getService() {
        return CoreService.INSTANCE;
    }
    
}
