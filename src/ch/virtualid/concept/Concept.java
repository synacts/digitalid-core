package ch.virtualid.concept;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import javax.annotation.Nonnull;
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
    
    /**
     * Returns whether this concept has an entity.
     * 
     * @return whether this concept has an entity.
     */
    @Pure
    public final boolean hasEntity() {
        return entity != null;
    }
    
    /**
     * Returns the entity to which this concept belongs.
     * 
     * @return the entity to which this concept belongs.
     * 
     * @require hasEntity() : "This concept has an entity.";
     */
    @Pure
    public final @Nonnull Entity getEntityNotNull() {
        assert entity != null : "This concept has an entity.";
        
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
