package ch.virtualid.entity;

import ch.virtualid.agent.ClientAgent;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Client;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.database.Database;
import static ch.virtualid.entity.Entity.CREATED;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.service.CoreService;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentMap;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a native role on the client-side.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class NativeRole extends Role implements Immutable {
    
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
     * Returns whether this role is accredited.
     * If it is, the current state is retrieved.
     * 
     * @return whether this role is accredited.
     */
    public boolean isAccredited() throws InterruptedException, SQLException, IOException, PacketException, ExternalException {
        try {
            reloadState(CoreService.SERVICE);
            return true;
        } catch (@Nonnull PacketException exception) {
            if (exception.getError() == PacketError.AUDIT) return false;
            else throw exception;
        }
    }
    
    
    /**
     * Caches the native roles given their client and number.
     */
    private static final @Nonnull ConcurrentMap<Client, ConcurrentMap<Long, NativeRole>> index = new ConcurrentHashMap<Client, ConcurrentMap<Long, NativeRole>>();
    
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
    public static @Nonnull NativeRole get(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, long agentNumber) throws SQLException {
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
     * (Use {@link Client#addRole(ch.virtualid.identity.InternalNonHostIdentity, long)} instead.)
     * 
     * @param client the client that can assume the returned role.
     * @param issuer the issuer of the returned role.
     * @param agentNumber the agent number of the returned role.
     * 
     * @return a new or existing native role with the given arguments.
     */
    public static @Nonnull NativeRole add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, long agentNumber) throws SQLException {
        final @Nonnull NativeRole role = get(client, RoleModule.map(client, issuer, null, null, agentNumber), issuer, agentNumber);
        role.notify(CREATED);
        return role;
    }
    
    @Override
    public void remove() throws SQLException {
        if (Database.isSingleAccess()) {
            final @Nullable ConcurrentMap<Long, NativeRole> map = index.get(getClient());
            if (map != null) map.remove(getNumber());
        }
        super.remove();
    }
    
}
