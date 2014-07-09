package ch.virtualid.database;

import ch.virtualid.identity.Identity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.server.Host;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an entity on the host-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class HostEntity extends Entity implements Immutable {
    
    /**
     * Stores the host of this entity.
     */
    private final @Nonnull Host host;
    
    /**
     * Stores the identity of this entity.
     */
    private final @Nonnull Identity identity;
    
    /**
     * Creates a new client entity with the given client and role.
     * 
     * @param host the host of this entity.
     * @param identity the identity of this entity.
     */
    public HostEntity(@Nonnull Host host, @Nonnull Identity identity) {
        this.host = host;
        this.identity = identity;
    }
    
    
    /**
     * Returns the host of this entity.
     * 
     * @return the host of this entity.
     */
    public @Nonnull Host getHost() {
        return host;
    }
    
    
    @Override
    public @Nonnull Host getSite() {
        return host;
    }
    
    @Override
    public @Nonnull Identity getIdentity() {
        return identity;
    }
    
    @Override
    public long getNumber() {
        return identity.getNumber();
    }
    
    @Override
    public @Nonnull String toString() {
        return identity.toString();
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + host.hashCode();
        hash = 41 * hash + identity.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof HostEntity)) return false;
        @Nonnull HostEntity other = (HostEntity) object;
        return this.host.equals(other.host) && this.identity.equals(other.identity);
    }
    
}
