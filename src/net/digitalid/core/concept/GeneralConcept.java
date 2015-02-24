package net.digitalid.core.concept;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;

/**
 * This class models a general concept.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
