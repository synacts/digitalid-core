package ch.virtualid.synchronizer;

import ch.virtualid.annotations.EndsCommitted;
import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.Action;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.io.Level;
import ch.virtualid.module.BothModule;
import ch.virtualid.packet.Packet;
import ch.virtualid.packet.Response;
import ch.virtualid.service.Service;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableHashSet;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.FreezableSet;
import ch.virtualid.util.ReadonlyList;
import ch.virtualid.util.ReadonlySet;
import ch.xdf.Block;
import ch.xdf.CompressionWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a response audit with the trail and the times of the last and this audit.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class ResponseAudit extends Audit implements Immutable, Blockable {
    
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
    private final @Nonnull ReadonlyList<Block> trail;
    
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
    public ResponseAudit(@Nonnull Time lastTime, @Nonnull Time thisTime, @Nonnull ReadonlyList<Block> trail) {
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
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
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
    public @Nonnull ReadonlyList<Block> getTrail() {
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
    static final @Nonnull ReadonlySet<BothModule> emptyModuleSet = new FreezableHashSet<BothModule>().freeze();
    
    /**
     * Stores an empty list of methods.
     */
    static final @Nonnull ReadonlyList<Method> emptyMethodList = new FreezableLinkedList<Method>().freeze();
    
    /**
     * Executes the trail of this audit.
     * 
     * @param role the role for which the trail is to be executed.
     * @param service the service whose trail is to be executed.
     * @param recipient the recipient of the actions in the trail.
     * @param methods the methods that were sent with the audit request.
     * @param ignoredModules the modules that are ignored when executing the trail.
     */
    @EndsCommitted
    void execute(@Nonnull Role role, @Nonnull Service service, @Nonnull HostIdentifier recipient, @Nonnull ReadonlyList<Method> methods, @Nonnull ReadonlySet<BothModule> ignoredModules) throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull FreezableSet<BothModule> suspendedModules = new FreezableHashSet<BothModule>();
        for (@Nonnull Block block : trail) {
            final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, true, role);
            final @Nonnull CompressionWrapper compression = new CompressionWrapper(signature.getElementNotNull());
            final @Nonnull Block element = new SelfcontainedWrapper(compression.getElementNotNull()).getElement();
            final @Nonnull Action action = Method.get(role, signature, recipient, element).toAction();
            Database.commit();
            
            final @Nonnull ReadonlyList<BothModule> suspendModules = action.suspendModules();
            if (suspendModules.isNotEmpty()) {
                suspendedModules.addAll((FreezableList<BothModule>) suspendModules);
            }
            
            final @Nonnull BothModule module = action.getModule();
            if (!suspendedModules.contains(module) && !ignoredModules.contains(module) && !methods.contains(action)) {
                try {
                    action.executeOnClient();
                } catch (@Nonnull SQLException exception) {
                    Synchronizer.LOGGER.log(Level.WARNING, "Could not execute an audited action on the client", exception);
                    Database.rollback();
                    
                    try {
                        final @Nonnull ReadonlyList<InternalAction> reversedActions = SynchronizerModule.reverseInterferingActions(action);
                        action.executeOnClient();
                        ActionModule.audit(action);
                        Database.commit();
                        SynchronizerModule.redoReversedActions(reversedActions);
                    } catch (@Nonnull SQLException e) {
                        Synchronizer.LOGGER.log(Level.WARNING, "Could not execute on the client after having reversed the interfering actions", e);
                        Database.rollback();
                        suspendedModules.add(module);
                    }
                }
            }
            
            ActionModule.audit(action);
            Database.commit();
        }
        
        SynchronizerModule.setLastTime(role, service, thisTime);
        Database.commit();
        
        suspendedModules.removeAll((FreezableSet<BothModule>) ignoredModules);
        if (suspendedModules.freeze().isNotEmpty()) {
            final @Nonnull FreezableList<Method> queries = new FreezableArrayList<Method>(suspendedModules.size());
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
     * Executes the trail of this audit asynchronously.
     * 
     * @param method the method that was sent.
     */
    public void executeAsynchronously(final @Nonnull Method method) {
        new Thread(new Runnable() {
            @Override
            @EndsCommitted
            public void run() {
                final @Nonnull Role role = method.getRole();
                final @Nonnull Service service = method.getService();
                
                try {
                    execute(role, service, method.getRecipient(), new FreezableArrayList<Method>(method).freeze(), emptyModuleSet);
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    Synchronizer.LOGGER.log(Level.WARNING, "Could not execute the audit of '" + method + "' asynchronously", exception);
                    try {
                        Database.rollback();
                    } catch (@Nonnull SQLException e) {
                        Synchronizer.LOGGER.log(Level.WARNING, "Could not rollback", e);
                    }
                }
                
                Synchronizer.resume(role, service);
            }
        }).start();
    }
    
}
