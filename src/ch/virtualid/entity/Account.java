package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalIdentity;
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
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Account extends Entity implements Immutable, SQLizable {
    
    // TODO: Also support single access to database by introducing an index!
    
    /**
     * Stores the host of this account.
     */
    private final @Nonnull Host host;
    
    /**
     * Stores the identity of this account.
     */
    private final @Nonnull InternalIdentity identity;
    
    /**
     * Creates a new account with the given client and role.
     * 
     * @param host the host of this account.
     * @param identity the identity of this account.
     */
    public Account(@Nonnull Host host, @Nonnull InternalIdentity identity) {
        this.host = host;
        this.identity = identity;
    }
    
    
    /**
     * Returns the host of this account.
     * 
     * @return the host of this account.
     */
    @Pure
    public @Nonnull Host getHost() {
        return host;
    }
    
    
    @Pure
    @Override
    public @Nonnull Host getSite() {
        return host;
    }
    
    @Pure
    @Override
    public @Nonnull InternalIdentity getIdentity() {
        return identity;
    }
    
    @Pure
    @Override
    public long getNumber() {
        return identity.getNumber();
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
    public static @Nonnull Account get(@Nonnull Host host, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull Identity identity = IdentityClass.get(resultSet, columnIndex);
        if (identity instanceof InternalIdentity) return new Account(host, (InternalIdentity) identity);
        else throw new SQLException("The identity of " + identity.getAddress() + " is not internal.");
    }
    
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + host.hashCode();
        hash = 41 * hash + identity.hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Account)) return false;
        final @Nonnull Account other = (Account) object;
        return this.host.equals(other.host) && this.identity.equals(other.identity);
    }
    
}
