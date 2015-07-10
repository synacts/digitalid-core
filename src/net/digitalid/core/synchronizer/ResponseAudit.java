package net.digitalid.core.synchronizer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableHashSet;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.FreezableSet;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.collections.ReadOnlySet;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.InternalAction;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.io.Level;
import net.digitalid.core.io.Logger;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.packet.Packet;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.Service;
import net.digitalid.core.thread.NamedThreadFactory;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.CompressionWrapper;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SelfcontainedWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class models a response audit with the trail and the times of the last and this audit.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
     * @invariant trail.isFrozen() : "The trail is frozen.";
     * @invariant trail.doesNotContainNull() : "The trail does not contain null.";
     * @invariant for (Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    private final @Nonnull ReadOnlyList<Block> trail;
    
    /**
     * Creates a new audit with the given times and trail.
     * 
     * @param lastTime the time of the last audit.
     * @param thisTime the time of this audit.
     * @param trail the trail of this audit.
     * 
     * @require trail.isFrozen() : "The trail is frozen.";
     * @require trail.doesNotContainNull() : "The trail does not contain null.";
     * @require for (Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    public ResponseAudit(@Nonnull Time lastTime, @Nonnull Time thisTime, @Nonnull ReadOnlyList<Block> trail) {
        super(lastTime);
        
        assert trail.isFrozen() : "The trail is frozen.";
        assert trail.doesNotContainNull() : "The trail does not contain null.";
        for (final @Nonnull Block block : trail) assert block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
        
        this.thisTime = thisTime;
        this.trail = trail;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<>(3);
        elements.set(0, getLastTime().toBlock().setType(Audit.LAST_TIME));
        elements.set(1, thisTime.toBlock().setType(Audit.THIS_TIME));
        elements.set(2, new ListWrapper(Audit.TRAIL, trail).toBlock());
        return new TupleWrapper(Audit.TYPE, elements.freeze()).toBlock();
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
     * @ensure trail.isFrozen() : "The trail is frozen.";
     * @ensure trail.doesNotContainNull() : "The trail does not contain null.";
     * @ensure for (Block block : return) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the returned trail is based on the packet signature type.";
     */
    @Pure
    public @Nonnull ReadOnlyList<Block> getTrail() {
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
    static final @Nonnull ReadOnlySet<BothModule> emptyModuleSet = new FreezableHashSet<BothModule>().freeze();
    
    /**
     * Stores an empty list of methods.
     */
    static final @Nonnull ReadOnlyList<Method> emptyMethodList = new FreezableLinkedList<Method>().freeze();
    
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
    void execute(@Nonnull Role role, @Nonnull Service service, @Nonnull HostIdentifier recipient, @Nonnull ReadOnlyList<Method> methods, @Nonnull ReadOnlySet<BothModule> ignoredModules) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull FreezableSet<BothModule> suspendedModules = new FreezableHashSet<>();
        for (@Nonnull Block block : trail) {
            final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, role);
            final @Nonnull CompressionWrapper compression = new CompressionWrapper(signature.getElementNotNull());
            final @Nonnull Block element = new SelfcontainedWrapper(compression.getElementNotNull()).getElement();
            final @Nonnull Action action = Method.get(role, signature, recipient, element).toAction();
            Database.commit();
            
            final @Nonnull ReadOnlyList<BothModule> suspendModules = action.suspendModules();
            if (suspendModules.isNotEmpty()) {
                suspendedModules.addAll((FreezableList<BothModule>) suspendModules);
            }
            
            final @Nonnull BothModule module = action.getModule();
            if (!suspendedModules.contains(module) && !ignoredModules.contains(module) && !methods.contains(action)) {
                try {
                    Logger.log(Level.DEBUGGING, "ResponseAudit", "Execute on the client the audited action " + action + ".");
                    action.executeOnClient();
                    ActionModule.audit(action);
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Logger.log(Level.WARNING, "ResponseAudit", "Could not execute on the client the audited action " + action + ".", exception);
                    Database.rollback();
                    
                    try {
                        final @Nonnull ReadOnlyList<InternalAction> reversedActions = SynchronizerModule.reverseInterferingActions(action);
                        Logger.log(Level.DEBUGGING, "ResponseAudit", "Execute on the client after having reversed the interfering actions the audited action " + action + ".");
                        action.executeOnClient();
                        ActionModule.audit(action);
                        Database.commit();
                        SynchronizerModule.redoReversedActions(reversedActions);
                    } catch (@Nonnull SQLException e) {
                        Logger.log(Level.WARNING, "ResponseAudit", "Could not execute on the client after having reversed the interfering actions the audited action " + action + ".", e);
                        suspendedModules.add(module);
                        Database.rollback();
                    }
                }
            } else {
                try {
                    Logger.log(Level.DEBUGGING, "ResponseAudit", "Add to the audit trail the ignored or already executed action " + action + ".");
                    ActionModule.audit(action);
                    Database.commit();
                } catch (@Nonnull SQLException exception) {
                    Logger.log(Level.WARNING, "ResponseAudit", "Could not add to the audit trail the ignored or already executed action " + action + ".", exception);
                    Database.rollback();
                }
            }
        }
        
        SynchronizerModule.setLastTime(role, service, thisTime);
        Database.commit();
        
        suspendedModules.removeAll((FreezableSet<BothModule>) ignoredModules);
        if (suspendedModules.freeze().isNotEmpty()) {
            final @Nonnull FreezableList<Method> queries = new FreezableArrayList<>(suspendedModules.size());
            for (final @Nonnull BothModule module : suspendedModules) queries.add(new StateQuery(role, module));
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
            Logger.log(Level.VERBOSE, "ResponseAudit", "Shutting down the response audit executor.");
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(1L, TimeUnit.MINUTES);
        } catch (@Nonnull InterruptedException exception) {
            Logger.log(Level.WARNING, "ResponseAudit", "Could not shut down the response audit executor.", exception);
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
                    Logger.log(Level.DEBUGGING, "ResponseAudit", "Execute asynchronously the audit of " + method + ".");
                    execute(role, service, method.getRecipient(), new FreezableArrayList<>(method).freeze(), emptyModuleSet);
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    Logger.log(Level.WARNING, "ResponseAudit", "Could not execute the audit of " + method + " asynchronously.", exception);
                    Database.rollback();
                } finally {
                    Database.unlock();
                }
                
                Synchronizer.resume(role, service);
            }
        });
    }
    
}
