package net.digitalid.core.entity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.ClientAgent;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.client.Client;
import net.digitalid.core.collections.ConcurrentHashMap;
import net.digitalid.core.collections.ConcurrentMap;
import net.digitalid.core.concept.Aspect;
import net.digitalid.core.concept.Instance;
import net.digitalid.core.concept.Observer;
import net.digitalid.core.database.Database;
import net.digitalid.core.identity.InternalNonHostIdentity;

/**
 * This class models a native role on the client-side.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
            if (map == null) map = index.putIfAbsentElseReturnPresent(client, new ConcurrentHashMap<Long, NativeRole>());
            @Nullable NativeRole role = map.get(number);
            if (role == null) role = map.putIfAbsentElseReturnPresent(number, new NativeRole(client, number, issuer, agentNumber));
            return role;
        } else {
            return new NativeRole(client, number, issuer, agentNumber);
        }
    }
    
    /**
     * Returns a new or existing native role with the given arguments.
     * <p>
     * <em>Important:</em> This method should not be called directly!
     * (Use {@link Client#addRole(net.digitalid.core.identity.InternalNonHostIdentity, long)} instead.)
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing native role with the given arguments.
     */
    @NonCommitting
    public static @Nonnull NativeRole add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, long agentNumber) throws SQLException {
        final @Nonnull NativeRole role = get(client, RoleModule.map(client, issuer, null, null, agentNumber), issuer, agentNumber);
        role.notify(CREATED);
        return role;
    }
    
    @Override
    @NonCommitting
    public void remove() throws SQLException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<Long, NativeRole> map = index.get(getClient());
            if (map != null) map.remove(getNumber());
        }
        super.remove();
    }
    
}
