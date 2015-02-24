package net.digitalid.core.synchronizer;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableHashSet;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.FreezableSet;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.collections.ReadonlySet;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.InternalAction;
import net.digitalid.core.handler.Method;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.interfaces.Blockable;
import net.digitalid.core.interfaces.Immutable;
import net.digitalid.core.io.Level;
import net.digitalid.core.module.BothModule;
import net.digitalid.core.packet.Packet;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.Service;
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
    @Committing
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
                    System.out.println("Execute the audited action: " + action); // TODO: Remove eventually!
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
                        suspendedModules.add(module);
                        Database.rollback();
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
            @Committing
            public void run() {
                final @Nonnull Role role = method.getRole();
                final @Nonnull Service service = method.getService();
                
                try {
                    Database.lock();
                    execute(role, service, method.getRecipient(), new FreezableArrayList<Method>(method).freeze(), emptyModuleSet);
                } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
                    Synchronizer.LOGGER.log(Level.WARNING, "Could not execute the audit of '" + method + "' asynchronously", exception);
                    Database.rollback();
                } finally {
                    Database.unlock();
                }
                
                Synchronizer.resume(role, service);
            }
        }).start();
    }
    
}
