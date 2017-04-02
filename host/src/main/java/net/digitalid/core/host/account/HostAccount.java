package net.digitalid.core.host.account;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identity.HostIdentity;

/**
 * This class models a host account.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class HostAccount extends Account implements HostEntity {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull HostIdentity getIdentity();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    public static @Nonnull HostAccount with(@Nonnull Host host, @Nonnull HostIdentity identity) {
        return new HostAccountSubclass(host, identity);
    }
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    // TODO: Figure out whether/how we can use the ConceptIndex for this.
    
//    /**
//     * Caches host accounts given their host and identity.
//     */
//    private static final @Nonnull ConcurrentMap<Host, ConcurrentMap<HostIdentity, HostAccount>> index = new ConcurrentHashMap<>();
//    
//    static {
//        if (Database.isSingleAccess()) {
//            Instance.observeAspects(new Observer() {
//                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
//            }, Host.DELETED);
//        }
//    }
//    
//    /**
//     * Returns a potentially locally cached host account.
//     * 
//     * @param host the host of the host account to return.
//     * @param identity the identity of the host account to return.
//     * 
//     * @return a new or existing host account with the given host and identity.
//     */
//    @Pure
//    public static @Nonnull HostAccount get(@Nonnull Host host, @Nonnull HostIdentity identity) {
//        if (Database.isSingleAccess()) {
//            @Nullable ConcurrentMap<HostIdentity, HostAccount> map = index.get(host);
//            if (map == null) { map = index.putIfAbsentElseReturnPresent(host, new ConcurrentHashMap<HostIdentity, HostAccount>()); }
//            @Nullable HostAccount account = map.get(identity);
//            if (account == null) { account = map.putIfAbsentElseReturnPresent(identity, new HostAccount(host, identity)); }
//            return account;
//        } else {
//            return new HostAccount(host, identity);
//        }
//    }
    
}
