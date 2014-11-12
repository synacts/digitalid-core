package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Host;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
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
    private Account(@Nonnull Host host, @Nonnull InternalIdentity identity) {
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
     * Notifies the observers that this account has been opened.
     */
    @Pure
    public void opened() {
        notify(CREATED);
    }
    
    /**
     * Notifies the observers that this account has been closed.
     */
    @Pure
    public void closed() {
        notify(DELETED);
    }
    
    
    /**
     * Caches accounts given their host and identity.
     */
    private static final @Nonnull ConcurrentMap<Host, ConcurrentMap<InternalIdentity, Account>> index = new ConcurrentHashMap<Host, ConcurrentMap<InternalIdentity, Account>>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Host.DELETED);
        }
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
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<InternalIdentity, Account> map = index.get(host);
            if (map == null) map = index.putIfAbsentElseReturnPresent(host, new ConcurrentHashMap<InternalIdentity, Account>());
            @Nullable Account account = map.get(identity);
            if (account == null) account = map.putIfAbsentElseReturnPresent(identity, new Account(host, identity));
            return account;
        } else {
            return new Account(host, identity);
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
    public static @Nonnull Account get(@Nonnull Host host, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, columnIndex);
        if (identity instanceof InternalIdentity) return get(host, (InternalIdentity) identity);
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
