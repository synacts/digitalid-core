package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.NonHostAccount;
import ch.virtualid.entity.NonHostEntity;
import javax.annotation.Nonnull;

/**
 * This class models a non-host concept.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class NonHostConcept extends Concept {
    
    /**
     * Stores the entity to which this concept belongs.
     */
    private final @Nonnull NonHostEntity entity;
    
    /**
     * Creates a new non-host concept with the given entity.
     * 
     * @param entity the entity to which this concept belongs.
     */
    protected NonHostConcept(@Nonnull NonHostEntity entity) {
        this.entity = entity;
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostEntity getEntity() {
        return entity;
    }
    
    @Pure
    @Override
    public final @Nonnull NonHostAccount getAccount() {
        assert isOnHost() : "This concept is on a host.";
        
        return (NonHostAccount) entity;
    }
    
}
