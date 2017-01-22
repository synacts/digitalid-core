package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;

/**
 * This class models a concept of the {@link CoreService core service}.
 */
@Immutable
public abstract class CoreServiceCoreSubject<ENTITY extends Entity<?>, KEY> extends CoreSubject<ENTITY, KEY> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Stores the service to which this concept belongs.
     */
    public static final @Nonnull Service SERVICE = CoreService.INSTANCE;
    
}
