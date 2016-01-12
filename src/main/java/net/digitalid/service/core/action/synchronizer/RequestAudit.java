package net.digitalid.service.core.action.synchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.handler.InternalMethod;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class models a request audit with the time of the last audit.
 */
@Immutable
public final class RequestAudit extends Audit {
    
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
        return "request audit from " + getLastTime().asDate();
    }
    
    
    /**
     * Returns the request audit for the given method.
     * 
     * @param method the method which is sent to a host.
     * 
     * @return the request audit for the given method.
     */
    @NonCommitting
    public static @Nullable RequestAudit get(@Nonnull Method method) throws DatabaseException {
        if (method.isOnClient() && method instanceof InternalMethod) {
            final @Nonnull Role role = method.getRole();
            final @Nonnull Service service = method.getService();
            if (Synchronizer.suspend(role, service)) { return new RequestAudit(SynchronizerModule.getLastTime(role, service)); }
        }
        return null;
    }
    
    /**
     * Releases the lock on the service of the given method.
     * 
     * @param method the method which was sent to a host.
     */
    public static void release(@Nonnull Method method) {
        if (method.isOnClient() && method instanceof InternalMethod) { Synchronizer.resume(method.getRole(), method.getService()); }
    }
    
}
