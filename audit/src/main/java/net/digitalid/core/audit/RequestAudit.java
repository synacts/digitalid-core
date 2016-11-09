package net.digitalid.core.audit;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models a request audit with the time of the last audit.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class RequestAudit extends Audit {
    
    // TODO: Move this logic to the synchronizer project or find another solution.
    
//    /**
//     * Returns the request audit for the given method.
//     * 
//     * @param method the method which is sent to a host.
//     * 
//     * @return the request audit for the given method.
//     */
//    @NonCommitting
//    public static @Nullable RequestAudit get(@Nonnull Method method) throws DatabaseException {
//        if (method.isOnClient() && method instanceof InternalMethod) {
//            final @Nonnull Role role = method.getRole();
//            final @Nonnull Service service = method.getService();
//            if (Synchronizer.suspend(role, service)) { return new RequestAudit(SynchronizerModule.getLastTime(role, service)); }
//        }
//        return null;
//    }
//    
//    /**
//     * Releases the lock on the service of the given method.
//     * 
//     * @param method the method which was sent to a host.
//     */
//    public static void release(@Nonnull Method method) {
//        if (method.isOnClient() && method instanceof InternalMethod) { Synchronizer.resume(method.getRole(), method.getService()); }
//    }
    
}
