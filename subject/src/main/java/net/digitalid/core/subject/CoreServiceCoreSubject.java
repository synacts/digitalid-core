package net.digitalid.core.subject;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;

/**
 * This class models a core subject of the {@link CoreService core service}.
 */
@Immutable
public abstract class CoreServiceCoreSubject<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY> extends CoreSubject<ENTITY, KEY> {
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Stores the service to which this core subject belongs.
     */
    public static final @Nonnull Service SERVICE = CoreService.INSTANCE;
    
}
