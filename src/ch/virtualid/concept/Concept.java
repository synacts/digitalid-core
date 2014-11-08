package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import javax.annotation.Nonnull;

/**
 * This class models an abstract {@link Concept concept} in the {@link Database database}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Concept extends Instance {
    
    /**
     * Stores the entity to which this concept belongs.
     */
    private final @Nonnull Entity entity;
    
    /**
     * Creates a new concept with the given entity.
     * 
     * @param entity the entity to which this concept belongs.
     */
    protected Concept(@Nonnull Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Returns the entity to which this concept belongs.
     * 
     * @return the entity to which this concept belongs.
     */
    @Pure
    public final @Nonnull Entity getEntity() {
        return entity;
    }
    
    /**
     * Returns whether this concept is on a host.
     * 
     * @return whether this concept is on a host.
     */
    @Pure
    public final boolean isOnHost() {
        return entity instanceof Account;
    }
    
    /**
     * Returns whether this concept is on a client.
     * 
     * @return whether this concept is on a client.
     */
    @Pure
    public final boolean isOnClient() {
        return entity instanceof Role;
    }
    
}
