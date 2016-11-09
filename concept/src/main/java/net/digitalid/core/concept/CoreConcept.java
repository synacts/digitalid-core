package net.digitalid.core.concept;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;

/**
 * This class models a concept of the {@link Service#CORE core service}.
 */
@Immutable
public abstract class CoreConcept<E extends Entity, K> extends Concept<E, K> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Stores the service to which this concept belongs.
     */
    public static final @Nonnull Service SERVICE = CoreService.INSTANCE;
    
}
