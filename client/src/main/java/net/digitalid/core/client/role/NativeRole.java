package net.digitalid.core.client.role;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.client.Client;
import net.digitalid.core.clientagent.ClientAgent;

/**
 * This class models a native role on the client.
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter
public abstract class NativeRole extends Role {
    
    /* -------------------------------------------------- Agent -------------------------------------------------- */
    
    @Pure
    @Override
    @Derive("ClientAgent.of(this, agentKey)")
    public abstract @Nonnull ClientAgent getAgent();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull NativeRole with(@Nonnull Client unit, long key) throws DatabaseException, RecoveryException {
        final @Nonnull Role role = Role.with(unit, key);
        if (role instanceof NativeRole) { return (NativeRole) role; }
        else { throw RecoveryExceptionBuilder.withMessage("The key " + key + " does not denote a native role.").build(); }
    }
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    // TODO: Figure out whether/how we can use the ConceptIndex for this.
    
//    /**
//     * Caches the native roles given their client and number.
//     */
//    private static final @Nonnull ConcurrentMap<Client, ConcurrentMap<Long, NativeRole>> index = new ConcurrentHashMap<>();
//    
//    static {
//        if (Database.isSingleAccess()) {
//            Instance.observeAspects(new Observer() {
//                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
//            }, Client.DELETED);
//        }
//    }
//    
//    /**
//     * Returns the potentially locally cached native role with the given arguments.
//     * 
//     * @param client the client that can assume the returned role.
//     * @param number the number that references the returned role.
//     * @param issuer the issuer of the returned role.
//     * @param agentNumber the agent number of the returned role.
//     * 
//     * @return a new or existing native role with the given arguments.
//     */
//    public static @Nonnull NativeRole get(@Nonnull Client client, long number, @Nonnull InternalNonHostIdentity issuer, long agentNumber) {
//        if (Database.isSingleAccess()) {
//            @Nullable ConcurrentMap<Long, NativeRole> map = index.get(client);
//            if (map == null) { map = index.putIfAbsentElseReturnPresent(client, new ConcurrentHashMap<Long, NativeRole>()); }
//            @Nullable NativeRole role = map.get(number);
//            if (role == null) { role = map.putIfAbsentElseReturnPresent(number, new NativeRole(client, number, issuer, agentNumber)); }
//            return role;
//        } else {
//            return new NativeRole(client, number, issuer, agentNumber);
//        }
//    }
//    
//    /**
//     * Returns a new or existing native role with the given arguments.
//     * <p>
//     * <em>Important:</em> This method should not be called directly!
//     * (Use {@link Client#addRole(net.digitalid.service.core.identity.InternalNonHostIdentity, long)} instead.)
//     * 
//     * @param client the client that can assume the returned role.
//     * @param issuer the issuer of the returned role.
//     * @param agentNumber the agent number of the returned role.
//     * 
//     * @return a new or existing native role with the given arguments.
//     */
//    @NonCommitting
//    public static @Nonnull NativeRole add(@Nonnull Client client, @Nonnull InternalNonHostIdentity issuer, long agentNumber) throws DatabaseException {
//        final @Nonnull NativeRole role = get(client, RoleModule.map(client, issuer, null, null, agentNumber), issuer, agentNumber);
//        role.notify(CREATED);
//        return role;
//    }
//    
//    @Override
//    @NonCommitting
//    public void remove() throws DatabaseException {
//        if (Database.isSingleAccess()) {
//            final @Nullable ConcurrentMap<Long, NativeRole> map = index.get(getClient());
//            if (map != null) { map.remove(getKey()); }
//        }
//        super.remove();
//    }
    
}
