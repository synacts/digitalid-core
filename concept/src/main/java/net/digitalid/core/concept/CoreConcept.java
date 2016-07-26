package net.digitalid.core.concept;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.Service;

/**
 * This class models a concept of the {@link Service#CORE core service}.
 */
@Immutable
public abstract class CoreConcept<E extends Entity, K> extends Concept<E, K> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return Service.CORE;
    }
    
}
