package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Host;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an account on the host-side.
 * 
 * @see HostAccount
 * @see NonHostAccount
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Account extends EntityClass implements Immutable, SQLizable {
    
    /**
     * Stores the host of this account.
     */
    private final @Nonnull Host host;
    
    /**
     * Creates a new account with the given host.
     * 
     * @param host the host of this account.
     */
    Account(@Nonnull Host host) {
        this.host = host;
    }
    
    
    /**
     * Returns the host of this account.
     * 
     * @return the host of this account.
     */
    @Pure
    public final @Nonnull Host getHost() {
        return host;
    }
    
    
    @Pure
    @Override
    public final @Nonnull Host getSite() {
        return host;
    }
    
    @Pure
    @Override
    public final long getNumber() {
        return getIdentity().getNumber();
    }
    
    
    /**
     * Notifies the observers that this account has been opened.
     */
    @Pure
    public void opened() {
        notify(Entity.CREATED);
    }
    
    /**
     * Notifies the observers that this account has been closed.
     */
    @Pure
    public void closed() {
        notify(Entity.DELETED);
    }
    
    
    /**
     * Returns a potentially locally cached account.
     * 
     * @param host the host of the account to return.
     * @param identity the identity of the account to return.
     * 
     * @return a new or existing account with the given host and identity.
     */
    @Pure
    public static @Nonnull Account get(@Nonnull Host host, @Nonnull InternalIdentity identity) {
        if (identity instanceof HostIdentity) {
            return HostAccount.get(host, (HostIdentity) identity);
        } else if (identity instanceof InternalNonHostIdentity) {
            return NonHostAccount.get(host, (InternalNonHostIdentity) identity);
        } else {
            throw new ShouldNeverHappenError("An internal identity is either a host or a non-host.");
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param host the host on which the account is hosted.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Account getNotNull(@Nonnull Host host, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, columnIndex);
        if (identity instanceof InternalIdentity) return get(host, (InternalIdentity) identity);
        else throw new SQLException("The identity of " + identity.getAddress() + " is not internal.");
    }
    
    
    @Pure
    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 41 * hash + host.hashCode();
        hash = 41 * hash + getIdentity().hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Account)) return false;
        final @Nonnull Account other = (Account) object;
        return this.host.equals(other.host) && this.getIdentity().equals(other.getIdentity());
    }
    
}
