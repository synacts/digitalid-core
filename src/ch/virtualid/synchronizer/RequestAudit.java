package ch.virtualid.synchronizer;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.InternalMethod;
import ch.virtualid.handler.Method;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.module.Service;
import ch.virtualid.util.ConcurrentHashMap;
import ch.virtualid.util.ConcurrentHashSet;
import ch.virtualid.util.ConcurrentMap;
import ch.virtualid.util.ConcurrentSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a request audit with the time of the last audit.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class RequestAudit extends Audit implements Immutable, Blockable {
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    RequestAudit(@Nonnull Time lastTime) {
        super(lastTime);
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Request audit from " + getLastTime().asDate();
    }
    
    
    /**
     * Stores the services that are currently suspended because an audit is already requested.
     * As this information is not stored in the database, it might happen that another process requests the same audit simultaneously.
     * (TODO: As long as the database connection is not committed until the new last time is written, this should not happen.)
     */
    private static final @Nonnull ConcurrentMap<Role, ConcurrentSet<Service>> suspendedServices = new ConcurrentHashMap<Role, ConcurrentSet<Service>>();
    
    /**
     * Returns the request audit for the given method.
     * 
     * @param method the method which is sent to a host.
     * 
     * @return the request audit for the given method.
     */
    public static @Nullable RequestAudit get(@Nonnull Method method) throws SQLException {
        if (method.isOnClient() && method instanceof InternalMethod && method.isSimilarTo(method)) {
            final @Nonnull Role role = method.getRole();
            final @Nonnull Service service = method.getService();
            @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
            if (set == null) set = suspendedServices.putIfAbsentElseReturnPresent(role, new ConcurrentHashSet<Service>());
            if (set.add(service)) return new RequestAudit(Synchronization.getLastTime(role, service));
        }
        return null;
    }
    
    /**
     * Releases the lock on the service of the given method.
     * 
     * @param method the method which was sent to a host.
     */
    public static void release(@Nonnull Method method) {
        if (method.isOnClient() && method instanceof InternalMethod && method.isSimilarTo(method)) {
            final @Nonnull Role role = method.getRole();
            final @Nullable ConcurrentSet<Service> set = suspendedServices.get(role);
            if (set != null) set.remove(method.getService());
        }
    }
    
}
