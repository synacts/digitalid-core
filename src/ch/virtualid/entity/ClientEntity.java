package ch.virtualid.entity;

import ch.virtualid.client.Client;
import ch.virtualid.client.Role;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an entity on the client-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class ClientEntity extends Entity implements Immutable {
    
    /**
     * Stores the client of this entity.
     */
    private final @Nonnull Client client;
    
    /**
     * Stores the role of this entity.
     */
    private final @Nonnull Role role;
    
    /**
     * Creates a new client entity with the given client and role.
     * 
     * @param client the client of this entity.
     * @param role the role of this entity.
     */
    public ClientEntity(@Nonnull Client client, @Nonnull Role role) {
        this.client = client;
        this.role = role;
    }
    
    
    /**
     * Returns the client of this entity.
     * 
     * @return the client of this entity.
     */
    public @Nonnull Client getClient() {
        return client;
    }
    
    /**
     * Returns the role of this entity.
     * 
     * @return the role of this entity.
     */
    public @Nonnull Role getRole() {
        return role;
    }
    
    
    @Override
    public @Nonnull Client getSite() {
        return client;
    }
    
    @Override
    public @Nonnull NonHostIdentity getIdentity() {
        return role.getIssuer();
    }
    
    @Override
    public long getNumber() {
        return role.getNumber();
    }
    
    @Override
    public @Nonnull String toString() {
        return role.toString();
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + client.hashCode();
        hash = 41 * hash + role.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof ClientEntity)) return false;
        @Nonnull ClientEntity other = (ClientEntity) object;
        return this.client.equals(other.client) && this.role.equals(other.role);
    }
    
}
