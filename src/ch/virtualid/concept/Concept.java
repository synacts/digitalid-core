package ch.virtualid.concept;

import ch.virtualid.annotations.OnlyForClients;
import ch.virtualid.annotations.OnlyForHosts;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import javax.annotation.Nonnull;

/**
 * This class models a concept in the {@link Database database}.
 * 
 * @see HostConcept
 * @see NonHostConcept
 * @see GeneralConcept
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Concept extends Instance {
    
    /**
     * Returns the entity to which this concept belongs.
     * 
     * @return the entity to which this concept belongs.
     */
    @Pure
    public abstract @Nonnull Entity getEntity();
    
    /**
     * Returns whether this concept is on a host.
     * 
     * @return whether this concept is on a host.
     */
    @Pure
    public final boolean isOnHost() {
        return getEntity() instanceof Account;
    }
    
    /**
     * Returns whether this concept is on a client.
     * 
     * @return whether this concept is on a client.
     */
    @Pure
    public final boolean isOnClient() {
        return getEntity() instanceof Role;
    }
    
    /**
     * Returns the account to which this concept belongs.
     * 
     * @return the account to which this concept belongs.
     */
    @Pure
    @OnlyForHosts
    public abstract @Nonnull Account getAccount();
    
    /**
     * Returns the role to which this concept belongs.
     * 
     * @return the role to which this concept belongs.
     */
    @Pure
    @OnlyForClients
    public final @Nonnull Role getRole() {
        return (Role) getEntity();
    }
    
}
