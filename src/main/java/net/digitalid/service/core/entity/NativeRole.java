package net.digitalid.service.core.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.concept.Aspect;
import net.digitalid.service.core.concept.Instance;
import net.digitalid.service.core.concept.Observer;
import net.digitalid.service.core.concepts.agent.ClientAgent;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.site.client.Client;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a native role on the client-side.
 */
@Immutable
public final class NativeRole extends Role {
    
    /**
     * Stores the client agent of this role.
     */
    private final @Nonnull ClientAgent clientAgent;
    
    /**
     * Creates a new role for the given client with the given number, issuer and agent number.
     * 
     * @param client the client that can assume the new role.
     * @param number the number that references the new role.
     * @param issuer the issuer of the new role.
     * @param agentNumber the agent number of the new role.
     */
    private NativeRole(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, long agentNumber) {
        super(client, number, issuer);
        
        this.clientAgent = ClientAgent.get(this, agentNumber, false);
    }
    
    
    @Pure
    @Override
    public @Nonnull ClientAgent getAgent() {
        return clientAgent;
    }
    
    
    /**
     * Caches the native roles given their client and number.
     */
    private static final @Nonnull ConcurrentMap<Client, ConcurrentMap<Long, NativeRole>> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Client.DELETED);
        }
    }
    
    /**
     * Returns the potentially locally cached native role with the given arguments.
     * 
     * @param client the client that can assume the returned role.
     * @param number the number that references the returned role.
     * @param issuer the issuer of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing native role with the given arguments.
     */
    public static @Nonnull NativeRole get(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, long agentNumber) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Long, NativeRole> map = index.get(client);
            if (map == null) { map = index.putIfAbsentElseReturnPresent(client, new ConcurrentHashMap<Long, NativeRole>()); }
            @Nullable NativeRole role = map.get(number);
            if (role == null) { role = map.putIfAbsentElseReturnPresent(number, new NativeRole(client, number, issuer, agentNumber)); }
            return role;
        } else {
            return new NativeRole(client, number, issuer, agentNumber);
        }
    }
    
    /**
     * Returns a new or existing native role with the given arguments.
     * <p>
     * <em>Important:</em> This method should not be called directly!
     * (Use {@link Client#addRole(net.digitalid.service.core.identity.InternalNonHostIdentity, long)} instead.)
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing native role with the given arguments.
     */
    @NonCommitting
    public static @Nonnull NativeRole add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, long agentNumber) throws AbortException {
        final @Nonnull NativeRole role = get(client, RoleModule.map(client, issuer, null, null, agentNumber), issuer, agentNumber);
        role.notify(CREATED);
        return role;
    }
    
    @Override
    @NonCommitting
    public void remove() throws AbortException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<Long, NativeRole> map = index.get(getClient());
            if (map != null) { map.remove(getKey()); }
        }
        super.remove();
    }
    
}
