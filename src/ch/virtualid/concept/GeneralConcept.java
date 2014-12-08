package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import javax.annotation.Nonnull;

/**
 * This class models a general concept.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class GeneralConcept extends Concept {
    
    /**
     * Stores the entity to which this concept belongs.
     */
    private final @Nonnull Entity entity;
    
    /**
     * Creates a new general concept with the given entity.
     * 
     * @param entity the entity to which this concept belongs.
     */
    protected GeneralConcept(@Nonnull Entity entity) {
        this.entity = entity;
    }
    
    @Pure
    @Override
    public final @Nonnull Entity getEntity() {
        return entity;
    }
    
    @Pure
    @Override
    public final @Nonnull Account getAccount() {
        assert isOnHost() : "This concept is on a host.";
        
        return (Account) getEntity();
    }
    
}
