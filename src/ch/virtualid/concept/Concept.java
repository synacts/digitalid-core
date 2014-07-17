package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import javax.annotation.Nullable;

/**
 * This class models an abstract {@link Concept concept} in the {@link Database database}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Concept extends Instance {
    
    /**
     * Stores the entity to which this concept belongs or null if it is impersonal.
     */
    private final @Nullable Entity entity;
    
    /**
     * Creates a new concept with the given entity.
     * 
     * @param entity the entity to which this concept belongs or null if it is impersonal.
     */
    protected Concept(@Nullable Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Returns the entity to which this concept belongs or null if it is impersonal.
     * 
     * @return the entity to which this concept belongs or null if it is impersonal.
     */
    @Pure
    public final @Nullable Entity getEntity() {
        return entity;
    }
    
}
