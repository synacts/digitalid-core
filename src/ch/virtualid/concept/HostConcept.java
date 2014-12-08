package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.HostAccount;
import ch.virtualid.entity.HostEntity;
import javax.annotation.Nonnull;

/**
 * This class models a host concept.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
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
