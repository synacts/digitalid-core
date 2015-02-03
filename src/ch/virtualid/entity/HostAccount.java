package ch.virtualid.entity;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import ch.virtualid.host.Host;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a host account.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class HostAccount extends Account implements HostEntity, Immutable, SQLizable {
    
    /**
     * Stores the identity of this host account.
     */
    private final @Nonnull HostIdentity identity;
    
    /**
     * Creates a new host account with the given host and identity.
     * 
     * @param host the host of this host account.
     * @param identity the identity of this host account.
     */
    private HostAccount(@Nonnull Host host, @Nonnull HostIdentity identity) {
        super(host);
        
        this.identity = identity;
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentity getIdentity() {
        return identity;
    }
    
    
    /**
     * Caches host accounts given their host and identity.
     */
    private static final @Nonnull ConcurrentMap<Host, ConcurrentMap<HostIdentity, HostAccount>> index = new ConcurrentHashMap<Host, ConcurrentMap<HostIdentity, HostAccount>>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Host.DELETED);
        }
    }
    
    /**
     * Returns a potentially locally cached host account.
     * 
     * @param host the host of the host account to return.
     * @param identity the identity of the host account to return.
     * 
     * @return a new or existing host account with the given host and identity.
     */
    @Pure
    public static @Nonnull HostAccount get(@Nonnull Host host, @Nonnull HostIdentity identity) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<HostIdentity, HostAccount> map = index.get(host);
            if (map == null) map = index.putIfAbsentElseReturnPresent(host, new ConcurrentHashMap<HostIdentity, HostAccount>());
            @Nullable HostAccount account = map.get(identity);
            if (account == null) account = map.putIfAbsentElseReturnPresent(identity, new HostAccount(host, identity));
            return account;
        } else {
            return new HostAccount(host, identity);
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param host the host on which the host account is hosted.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull HostAccount getNotNull(@Nonnull Host host, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, columnIndex);
        if (identity instanceof HostIdentity) return get(host, (HostIdentity) identity);
        else throw new SQLException("The identity of " + identity.getAddress() + " is not a host.");
    }
    
}
