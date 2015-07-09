package net.digitalid.core.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.ConcurrentMap;
import net.digitalid.core.concept.Aspect;
import net.digitalid.core.concept.Instance;
import net.digitalid.core.concept.Observer;
import net.digitalid.core.database.Database;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.interfaces.SQLizable;

/**
 * This class models a non-host account.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class NonHostAccount extends Account implements NonHostEntity, SQLizable {
    
    /**
     * Stores the identity of this non-host account.
     */
    private final @Nonnull InternalNonHostIdentity identity;
    
    /**
     * Creates a new non-host account with the given host and identity.
     * 
     * @param host the host of this non-host account.
     * @param identity the identity of this non-host account.
     */
    private NonHostAccount(@Nonnull Host host, @Nonnull InternalNonHostIdentity identity) {
        super(host);
        
        this.identity = identity;
    }
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity() {
        return identity;
    }
    
    
    /**
     * Caches non-host accounts given their host and identity.
     */
    private static final @Nonnull ConcurrentMap<Host, ConcurrentMap<InternalNonHostIdentity, NonHostAccount>> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Host.DELETED);
        }
    }
    
    /**
     * Returns a potentially locally cached non-host account.
     * 
     * @param host the host of the non-host account to return.
     * @param identity the identity of the non-host account to return.
     * 
     * @return a new or existing non-host account with the given host and identity.
     */
    @Pure
    public static @Nonnull NonHostAccount get(@Nonnull Host host, @Nonnull InternalNonHostIdentity identity) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<InternalNonHostIdentity, NonHostAccount> map = index.get(host);
            if (map == null) map = index.putIfAbsentElseReturnPresent(host, new ConcurrentHashMap<InternalNonHostIdentity, NonHostAccount>());
            @Nullable NonHostAccount account = map.get(identity);
            if (account == null) account = map.putIfAbsentElseReturnPresent(identity, new NonHostAccount(host, identity));
            return account;
        } else {
            return new NonHostAccount(host, identity);
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param host the host on which the non-host account is hosted.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull NonHostAccount getNotNull(@Nonnull Host host, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, columnIndex);
        if (identity instanceof InternalNonHostIdentity) return get(host, (InternalNonHostIdentity) identity);
        else throw new SQLException("The identity of " + identity.getAddress() + " is not a non-host.");
    }
    
}
