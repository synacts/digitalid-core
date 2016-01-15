package net.digitalid.service.core.entity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.concept.Aspect;
import net.digitalid.service.core.concept.Instance;
import net.digitalid.service.core.concept.Observer;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.IdentityImplementation;
import net.digitalid.service.core.site.host.Host;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;

/**
 * This class models a host account.
 */
@Immutable
public final class HostAccount extends Account implements HostEntity {
    
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
    private static final @Nonnull ConcurrentMap<Host, ConcurrentMap<HostIdentity, HostAccount>> index = new ConcurrentHashMap<>();
    
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
            if (map == null) { map = index.putIfAbsentElseReturnPresent(host, new ConcurrentHashMap<HostIdentity, HostAccount>()); }
            @Nullable HostAccount account = map.get(identity);
            if (account == null) { account = map.putIfAbsentElseReturnPresent(identity, new HostAccount(host, identity)); }
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
    public static @Nonnull HostAccount getNotNull(@Nonnull Host host, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, columnIndex);
        if (identity instanceof HostIdentity) { return get(host, (HostIdentity) identity); }
        else { throw new SQLException("The identity of " + identity.getAddress() + " is not a host."); }
    }
    
}
