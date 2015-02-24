package net.digitalid.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.entity.HostAccount;
import net.digitalid.core.entity.HostEntity;

/**
 * This class models a host concept.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class HostConcept extends Concept {
    
    /**
     * Stores the entity to which this concept belongs.
     */
    private final @Nonnull HostEntity entity;
    
    /**
     * Creates a new host concept with the given entity.
     * 
     * @param entity the entity to which this concept belongs.
     */
    protected HostConcept(@Nonnull HostEntity entity) {
        this.entity = entity;
    }
    
    @Pure
    @Override
    public final @Nonnull HostEntity getEntity() {
        return entity;
    }
    
    @Pure
    @Override
    public final @Nonnull HostAccount getAccount() {
        return (HostAccount) entity;
    }
    
}
