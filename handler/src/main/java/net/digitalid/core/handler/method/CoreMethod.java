package net.digitalid.core.handler.method;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.service.CoreService;

/**
 * This interface is implemented by all methods of the core service.
 * 
 * @see CoreService
 */
@Immutable
public interface CoreMethod<ENTITY extends Entity> extends Method<ENTITY>, CoreHandler<ENTITY> {
    
    @Pure
    @Override
    @OrderOfAssignment(2)
    @Derive("subject.getHostIdentifier()")
    public abstract @Nonnull HostIdentifier getRecipient();
    
}
