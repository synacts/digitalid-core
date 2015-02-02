package ch.virtualid.synchronizer;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.entity.Role;
import ch.virtualid.handler.InternalMethod;
import ch.virtualid.handler.Method;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.service.Service;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a request audit with the time of the last audit.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
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
     * Returns the request audit for the given method.
     * 
     * @param method the method which is sent to a host.
     * 
     * @return the request audit for the given method.
     */
    public static @Nullable RequestAudit get(@Nonnull Method method) throws SQLException {
        if (method.isOnClient() && method instanceof InternalMethod) {
            final @Nonnull Role role = method.getRole();
            final @Nonnull Service service = method.getService();
            if (Synchronizer.suspend(role, service)) return new RequestAudit(SynchronizerModule.getLastTime(role, service));
        }
        return null;
    }
    
    /**
     * Releases the lock on the service of the given method.
     * 
     * @param method the method which was sent to a host.
     */
    public static void release(@Nonnull Method method) {
        if (method.isOnClient() && method instanceof InternalMethod) Synchronizer.resume(method.getRole(), method.getService());
    }
    
}
