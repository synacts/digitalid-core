package net.digitalid.service.core.action.synchronizer;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.CompressionWrapper;
import net.digitalid.service.core.block.wrappers.SelfcontainedWrapper;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.service.core.block.wrappers.structure.ListWrapper;
import net.digitalid.service.core.block.wrappers.structure.TupleWrapper;
import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.InternalAction;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.packet.Packet;
import net.digitalid.service.core.packet.Response;
import net.digitalid.service.core.storage.Service;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableHashSet;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.freezable.FreezableSet;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.collections.readonly.ReadOnlySet;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.system.thread.NamedThreadFactory;

/**
 * This class models a response audit with the trail and the times of the last and this audit.
 */
@Immutable
public final class ResponseAudit extends Audit {
    
    /**
     * Stores the time of this audit.
     */
    private final @Nonnull Time thisTime;
    
    /**
     * Stores the trail of this audit.
     * 
     * @invariant for (Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    private final @Nonnull @Frozen @NonNullableElements ReadOnlyList<Block> trail;
    
    /**
     * Creates a new audit with the given times and trail.
     * 
     * @param lastTime the time of the last audit.
     * @param thisTime the time of this audit.
     * @param trail the trail of this audit.
     * 
     * @require for (Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    public ResponseAudit(@Nonnull Time lastTime, @Nonnull Time thisTime, @Nonnull @Frozen @NonNullableElements ReadOnlyList<Block> trail) {
        super(lastTime);
        
        assert trail.isFrozen() : "The trail is frozen.";
        assert !trail.containsNull() : "The trail does not contain null.";
        for (final @Nonnull Block block : trail) { assert block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type."; }
        
        this.thisTime = thisTime;
        this.trail = trail;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(3);
        elements.set(0, getLastTime().toBlock().setType(Audit.LAST_TIME));
        elements.set(1, thisTime.toBlock().setType(Audit.THIS_TIME));
        elements.set(2, ListWrapper.encode(Audit.TRAIL, trail));
        return TupleWrapper.encode(Audit.TYPE, elements.freeze());
    }
    
    
    /**
     * Returns the time of this audit.
     * 
     * @return the time of this audit.
     */
    @Pure
    public @Nonnull Time getThisTime() {
        return thisTime;
    }
    
    /**
     * Returns the trail of this audit.
     * 
     * @return the trail of this audit.
     * 
     * @ensure for (Block block : return) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the returned trail is based on the packet signature type.";
     */
    @Pure
    public @Nonnull @Frozen @NonNullableElements ReadOnlyList<Block> getTrail() {
        return trail;
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Response audit from " + getLastTime().asDate() + " to " + thisTime.asDate() + " with " + trail.size() + " actions";
    }
    
    
    /**
     * Stores an empty set of modules.
     */
    static final @Nonnull ReadOnlySet<StateModule> emptyModuleSet = FreezableHashSet.<StateModule>get().freeze();
    
    /**
     * Stores an empty list of methods.
     */
    static final @Nonnull ReadOnlyList<Method> emptyMethodList = FreezableLinkedList.<Method>get().freeze();
    
    /**
     * Executes the trail of this audit.
     * 
     * @param role the role for which the trail is to be executed.
     * @param service the service whose trail is to be executed.
     * @param recipient the recipient of the actions in the trail.
     * @param methods the methods that were sent with the audit request.
     * @param ignoredModules the modules that are ignored when executing the trail.
     */
    @Committing
    void execute(@Nonnull Role role, @Nonnull Service service, @Nonnull HostIdentifier recipient, @Nonnull ReadOnlyList<Method> methods, @Nonnull ReadOnlySet<StateModule> ignoredModules) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull FreezableSet<StateModule> suspendedModules = FreezableHashSet.get();
        for (final @Nonnull Block block : trail) {
            final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, role);
            final @Nonnull Block element = SelfcontainedWrapper.decodeNonNullable(CompressionWrapper.decompressNonNullable(signature.getNonNullableElement()));
            final @Nonnull Action action = Method.get(role, signature, recipient, element).castTo(Action.class);
            Database.commit();
            
            final @Nonnull ReadOnlyList<StateModule> suspendModules = action.suspendModules();
            if (!suspendModules.isEmpty()) {
                suspendedModules.addAll((FreezableList<StateModule>) suspendModules);
            }
            
            final @Nonnull StateModule module = action.getModule();
            if (!suspendedModules.contains(module) && !ignoredModules.contains(module) && !methods.contains(action)) {
                try {
                    Log.debugging("Execute on the client the audited action " + action + ".");
                    action.executeOnClient();
                    ActionModule.audit(action);
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Log.warning("Could not execute on the client the audited action " + action + ".", exception);
                    Database.rollback();
                    
                    try {
                        final @Nonnull ReadOnlyList<InternalAction> reversedActions = SynchronizerModule.reverseInterferingActions(action);
                        Log.debugging("Execute on the client after having reversed the interfering actions the audited action " + action + ".");
                        action.executeOnClient();
                        ActionModule.audit(action);
                        Database.commit();
                        SynchronizerModule.redoReversedActions(reversedActions);
                    } catch (@Nonnull SQLException e) {
                        Log.warning("Could not execute on the client after having reversed the interfering actions the audited action " + action + ".", e);
                        suspendedModules.add(module);
                        Database.rollback();
                    }
                }
            } else {
                try {
                    Log.debugging("Add to the audit trail the ignored or already executed action " + action + ".");
                    ActionModule.audit(action);
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Log.warning("Could not add to the audit trail the ignored or already executed action " + action + ".", exception);
                    Database.rollback();
                }
            }
        }
        
        SynchronizerModule.setLastTime(role, service, thisTime);
        Database.commit();
        
        suspendedModules.removeAll((FreezableSet<StateModule>) ignoredModules);
        if (!suspendedModules.freeze().isEmpty()) {
            final @Nonnull FreezableList<Method> queries = FreezableArrayList.getWithCapacity(suspendedModules.size());
            for (final @Nonnull StateModule module : suspendedModules) { queries.add(new StateQuery(role, module)); }
            final @Nonnull Response response = Method.send(queries.freeze(), new RequestAudit(SynchronizerModule.getLastTime(role, service)));
            for (int i = 0; i < response.getSize(); i++) {
                final @Nonnull StateReply reply = response.getReplyNotNull(i);
                reply.updateState();
            }
            final @Nullable InternalAction lastAction = SynchronizerModule.pendingActions.peekLast();
            Database.commit();
            SynchronizerModule.redoPendingActions(role, suspendedModules, lastAction);
            response.getAuditNotNull().execute(role, service, recipient, emptyMethodList, suspendedModules);
        }
    }
    
    /**
     * The thread pool executor executes the audit asynchronously.
     */
    private static final @Nonnull ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8), new NamedThreadFactory("Audit"), new ThreadPoolExecutor.CallerRunsPolicy());
    
    /**
     * Shuts down the response audit executor after having finished the current audits.
     */
    public static void shutDown() {
        try {
            Log.verbose("Shutting down the response audit executor.");
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(1L, TimeUnit.MINUTES);
        } catch (@Nonnull InterruptedException exception) {
            Log.warning("Could not shut down the response audit executor.", exception);
        }
    }
    
    /**
     * Executes the trail of this audit asynchronously.
     * 
     * @param method the method that was sent.
     */
    public void executeAsynchronously(final @Nonnull Method method) {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            @Committing
            public void run() {
                final @Nonnull Role role = method.getRole();
                final @Nonnull Service service = method.getService();
                
                try {
                    Database.lock();
                    Log.debugging("Execute asynchronously the audit of " + method + ".");
                    execute(role, service, method.getRecipient(), FreezableArrayList.get(method).freeze(), emptyModuleSet);
                } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
                    Log.warning("Could not execute the audit of " + method + " asynchronously.", exception);
                    Database.rollback();
                } finally {
                    Database.unlock();
                }
                
                Synchronizer.resume(role, service);
            }
        });
    }
    
}
